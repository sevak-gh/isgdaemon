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
            return;
        }

        MCIProxy mciProxy = new MCIProxyImpl(mciUrl, mciUsername, mciPassword, mciNamespace);
        for (Transaction transaction : transactions) {
            try {
                MCIProxyGetTokenResponse getTokenResponse = mciProxy.getToken();
                if (getTokenResponse.getToken() == null) {
                    LOG.info("invalid token for verify from MCI");
                    continue;
                }
                MCIProxyRechargeVerifyResponse response = mciProxy.rechargeVerify(getTokenResponse.getToken(),
                        transaction.getConsumer(),
                        transaction.getId());
                if (response.getCode() == null) {
                    LOG.info("invalid response for verify from MCI");
                    continue;
                }
                if (Integer.parseInt(response.getCode()) == 0) {
                    transaction.setStf((Integer.parseInt(response.getTrCode()) == 0) ? 2 : 3);
                    transaction.setOperatorResponseCode(Integer.parseInt(response.getTrCode()));
                    transaction.setOperatorResponse(response.getTrDetail());
                    transaction.setOperatorTId(response.getTrSerial());
                    transactionRepository.update(transaction);
                    LOG.info("MCI recharge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                             transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                             transaction.getOperatorResponseCode(), transaction.getOperatorTId());
                } else {
                    LOG.info("MCI recharge verify[{},{}] was not successful", transaction.getConsumer(), transaction.getId());
                }
            } catch (ProxyAccessException e) {
                LOG.error("unable to VERIFY MCI recharge, verify canceled", e);
            }
        }
    }

    @Override
    public void mtnVerify() {
        // get MTN transactions set for STF, waiting to be verified
        List<Transaction> transactions = transactionRepository.findBySTFProvider(1, Operator.MTN_ID);

        if (transactions == null) {
            // nothing to be verified
            return;
        }

        MTNProxy mtnProxy = new MTNProxyImpl(mtnUrl, mtnUsername, mtnPassword, mtnVendor, mtnNamespace);
        for (Transaction transaction : transactions) {
            try {
                MTNProxyResponse response = mtnProxy.verify(transaction.getId());
                transaction.setStf((isMTNTransactionVerified(response.getResultCode(), response.getOrigResponseMessage())) ? 2 : 3);
                transaction.setOperatorResponseCode(Integer.parseInt(response.getResultCode()));
                transaction.setOperatorResponse(response.getOrigResponseMessage());
                transaction.setOperatorTId(response.getTransactionId());
                transaction.setOperatorCommand(response.getCommandStatus());
                transactionRepository.update(transaction);
                LOG.info("MTN recharge verify[{},{}] resolved: [STF={}, code={}, serial={}]",
                         transaction.getConsumer(), transaction.getId(), transaction.getStf(),
                         transaction.getOperatorResponseCode(), transaction.getOperatorTId());
            } catch (ProxyAccessException e) {
                LOG.error("unable to VERIFY MTN recharge, verify canceled", e);
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
        if (Integer.parseInt(tokens[1].trim()) == 1) {
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
