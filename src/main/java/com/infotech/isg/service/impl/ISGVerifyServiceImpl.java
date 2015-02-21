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


import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ISGVerifyServiceImpl(@Qualifier("JdbcTransactionRepository") TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void mciVerify() {

        // get MCI transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findBySTFProvider(1, Operator.MCI_ID);

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
                    transactionRepository.update(transaction);
                    AUDITLOG.info("MCI recharge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                                  transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                                  transaction.getOperatorResponseCode(), transaction.getOperatorTId());
                } else if (response.getCode().equals("-1")) {
                    // means transaction not registered
                    transaction.setStf(3);
                    transaction.setOperatorResponseCode(Integer.parseInt(response.getCode()));
                    transaction.setOperatorResponse(response.getDetail());
                    transactionRepository.update(transaction);
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
    public void mtnVerify() {
        // get MTN transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findBySTFProvider(1, Operator.MTN_ID);

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
                transactionRepository.update(transaction);
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
    public void jiringVerify() {
        //TODO: to be implemented
    }
}
