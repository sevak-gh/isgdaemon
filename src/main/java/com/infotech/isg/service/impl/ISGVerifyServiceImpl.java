package com.infotech.isg.service.impl;

import com.infotech.isg.domain.Transaction;
import com.infotech.isg.domain.Operator;
import com.infotech.isg.service.ISGVerifyService;
import com.infotech.isg.service.ISGException;
import com.infotech.isg.repository.TransactionRepository;
import com.infotech.isg.proxy.ProxyAccessException;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyImpl;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyRechargeVerifyResponse;
import com.infotech.isg.proxy.mtn.MTNProxy;
import com.infotech.isg.proxy.mtn.MTNProxyImpl;
import com.infotech.isg.proxy.mtn.MTNProxyResponse;
import com.infotech.isg.proxy.rightel.RightelProxy;
import com.infotech.isg.proxy.rightel.RightelProxyImpl;
import com.infotech.isg.proxy.rightel.RightelProxyInquiryChargeResponse;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implementation for ISG verify service
 *
 * @author Sevak Gharibian
 */
@Service("ISGVerifyService")
public class ISGVerifyServiceImpl implements ISGVerifyService {
    private static final Logger LOG = LoggerFactory.getLogger(ISGVerifyServiceImpl.class);
    private static final Logger AUDITLOG = LoggerFactory.getLogger("isgdaemon.audit");

    private final TransactionRepository transactionRepository;

    @Value("${mci2.url}")
    private String mciUrl;

    @Value("${mci2.username}")
    private String mciUsername;

    @Value("${mci2.password}")
    private String mciPassword;

    @Value("${mci2.namespace}")
    private String mciNamespace;

    @Value("${mtn.url}")
    private String mtnUrl;

    @Value("${mtn.username}")
    private String mtnUsername;

    @Value("${mtn.password}")
    private String mtnPassword;

    @Value("${mtn.vendor}")
    private String mtnVendor;

    @Value("${mtn.namespace}")
    private String mtnNamespace;

    @Value("${rightel.url}")
    private String rightelUrl;

    @Value("${rightel.username}")
    private String rightelUsername;

    @Value("${rightel.password}")
    private String rightelPassword;

    @Value("${rightel.namespace}")
    private String rightelNamespace;

    @Autowired
    public ISGVerifyServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void mciVerify() {

        // get MCI transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findByStfProvider(1, Operator.MCI_ID);

        if (transactions == null) {
            // nothing to be verified
            AUDITLOG.info("no MCI transaction to verify");
            return;
        }

        MCIProxy mciProxy = new MCIProxyImpl(mciUrl, mciUsername, mciPassword, mciNamespace);
        for (Transaction transaction : transactions) {
            try {
                MCIProxyGetTokenResponse getTokenResponse = mciProxy.getToken();
                if (getTokenResponse.getToken() == null) {
                    LOG.error("invalid token from MCI to verify({},{}), try again", transaction.getConsumer(), transaction.getId());
                    AUDITLOG.info("MCI recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
                    continue;
                }
                MCIProxyRechargeVerifyResponse response = mciProxy.rechargeVerify(getTokenResponse.getToken(),
                        transaction.getConsumer(),
                        transaction.getId());
                if (response.getCode() == null) {
                    LOG.error("invalid response from MCI to verify({},{}), try again", transaction.getConsumer(), transaction.getId());
                    AUDITLOG.info("MCI recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
                    continue;
                }
                if (response.getCode().equals("0")) {
                    transaction.setStf((response.getTrCode().equals("0")) ? 2 : 3);
                    transaction.setOperatorResponseCode(Integer.parseInt(response.getTrCode()));
                    transaction.setOperatorResponse(response.getTrDetail());
                    transaction.setOperatorTId(response.getTrSerial());
                    transactionRepository.save(transaction);
                    AUDITLOG.info("MCI recharge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                                  transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                                  transaction.getOperatorResponseCode(), transaction.getOperatorTId());
                } else if (response.getCode().equals("-1")) {
                    // means transaction not registered
                    transaction.setStf(3);
                    transaction.setOperatorResponseCode(Integer.parseInt(response.getCode()));
                    transaction.setOperatorResponse(response.getDetail());
                    transactionRepository.save(transaction);
                    AUDITLOG.info("MCI recharge verify[{},{}] resolved: [STF={}, code={}, detail={}]",
                                  transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                                  transaction.getOperatorResponseCode(), transaction.getOperatorResponse());

                } else {
                    // resule code not (0,-1), try again
                    LOG.debug("MCI recharge verify[{},{}] reponse({}) not successful, try again",
                              transaction.getConsumer(), transaction.getId(), response.getCode());
                    AUDITLOG.info("MCI recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
                }
            } catch (ProxyAccessException e) {
                LOG.error("error to VERIFY MCI recharge({},{}), try again", transaction.getConsumer(), transaction.getId(), e);
                AUDITLOG.info("MCI recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
            }
        }
    }

    @Override
    @Transactional
    public void mtnVerify() {
        // get MTN transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findByStfProvider(1, Operator.MTN_ID);

        if (transactions == null) {
            // nothing to be verified
            AUDITLOG.info("no MTN transaction to verify");
            return;
        }

        MTNProxy mtnProxy = new MTNProxyImpl(mtnUrl, mtnUsername, mtnPassword, mtnVendor, mtnNamespace);
        for (Transaction transaction : transactions) {
            try {
                MTNProxyResponse response = mtnProxy.verify(transaction.getId());
                if ((response.getResultCode() == null)
                    || (response.getOrigResponseMessage() == null)) {
                    LOG.error("invalid response from MTN to verify({},{}), try again", transaction.getConsumer(), transaction.getId());
                    AUDITLOG.info("MTN recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
                    continue;
                }
                transaction.setStf((isMTNTransactionVerified(response.getResultCode(), response.getOrigResponseMessage())) ? 2 : 3);
                transaction.setOperatorResponseCode(Integer.parseInt(response.getResultCode()));
                transaction.setOperatorResponse(response.getOrigResponseMessage());
                transaction.setOperatorTId(response.getTransactionId());
                transaction.setOperatorCommand(response.getCommandStatus());
                transactionRepository.save(transaction);
                AUDITLOG.info("MTN recharge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                              transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                              transaction.getOperatorResponseCode(), transaction.getOperatorTId());
            } catch (ProxyAccessException e) {
                LOG.error("error to VERIFY MTN recharge({},{}), try again", transaction.getConsumer(), transaction.getId(), e);
                AUDITLOG.info("MTN recharge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
            }
        }
    }

    private boolean isMTNTransactionVerified(String resultCode, String originMessage) {
        if (!resultCode.equals("0")) {
            return false;
        }

        Pattern pattern = Pattern.compile("PROCESS: [0-9]+");
        Matcher m = pattern.matcher(originMessage);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group());
        }
        String process = sb.toString();
        String[] tokens = process.split(":");
        if ((tokens == null) || (tokens.length < 2)) {
            return false;
        }
        if (tokens[1].trim().equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void jiringVerify() {
        //TODO: to be implemented
    }
    
    @Override
    @Transactional
    public void rightelVerify() {

        // get Rightel transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findByStfProvider(1, Operator.RIGHTEL_ID);

        if (transactions == null) {
            // nothing to be verified
            AUDITLOG.info("no Rightel transaction to verify");
            return;
        }

        RightelProxy rightelProxy = new RightelProxyImpl(rightelUrl, rightelUsername, rightelPassword, rightelNamespace);
        for (Transaction transaction : transactions) {
            try {
                RightelProxyInquiryChargeResponse response = rightelProxy.inquiryCharge(transaction.getId());
                if ((response.getErrorCode() == 0)
                    && ((response.getStatus() == 6) 
                        || (response.getStatus() == 4)
                        || (response.getStatus() == 7))) {
                    transaction.setStf((response.getStatus() == 6) ? 3 : 2);
                    transaction.setOperatorResponseCode(response.getStatus());
                    transaction.setOperatorResponse(response.getChargeResponseDesc());
                    transaction.setOperatorTId(response.getRequestId());
                    transactionRepository.save(transaction);
                    AUDITLOG.info("Rightel charge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                                  transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                                  transaction.getOperatorResponseCode(), transaction.getOperatorTId());
                } else {
                    // not verified, try again
                    LOG.debug("Rightel charge not verified[{},{}] reponse({}), try again",
                              transaction.getConsumer(), transaction.getId(), response.getErrorCode());
                    AUDITLOG.info("Rightel charge not verified({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
                }
            } catch (ProxyAccessException e) {
                LOG.error("error to VERIFY Rightel charge({},{}), try again", transaction.getConsumer(), transaction.getId(), e);
                AUDITLOG.info("Rightel charge verify({},{}) failed, try again", transaction.getConsumer(), transaction.getId());
            }
        }
    }        
}
