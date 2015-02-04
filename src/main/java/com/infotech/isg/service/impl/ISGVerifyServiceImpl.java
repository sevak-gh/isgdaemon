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

import java.util.Date;
import java.util.List;

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
    private String url;

    @Value("${mci2.username}")
    private String username;

    @Value("${mci2.password}")
    private String password;

    @Value("${mci2.namespace}")
    private String namespace;

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

        MCIProxy mciProxy = new MCIProxyImpl(url, username, password, namespace);
        for (Transaction transaction : transactions) {
            try {
                MCIProxyGetTokenResponse getTokenResponse = mciProxy.getToken();
                MCIProxyRechargeVerifyResponse response = mciProxy.rechargeVerify(getTokenResponse.getToken(),
                        transaction.getConsumer(),
                        transaction.getId());
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
        //TODO: to be implemented
    }

    @Override
    public void jiringVerify() {
        //TODO: to be implemented
    }
}
