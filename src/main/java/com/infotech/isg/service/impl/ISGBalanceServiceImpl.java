package com.infotech.isg.service.impl;

import com.infotech.isg.service.ISGBalanceService;
import com.infotech.isg.service.ISGException;
import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.repository.BalanceLogRepository;
import com.infotech.isg.proxy.ProxyAccessException;
import com.infotech.isg.domain.BalanceLog;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyImpl;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyGetRemainedBrokerRechargeResponse;
import com.infotech.isg.proxy.mtn.MTNProxy;
import com.infotech.isg.proxy.mtn.MTNProxyImpl;
import com.infotech.isg.proxy.mtn.MTNProxyResponse;
import com.infotech.isg.proxy.jiring.JiringProxy;
import com.infotech.isg.proxy.jiring.JiringProxyImpl;
import com.infotech.isg.proxy.jiring.TCSRequest;
import com.infotech.isg.proxy.jiring.TCSResponse;
import com.infotech.isg.proxy.rightel.RightelProxy;
import com.infotech.isg.proxy.rightel.RightelProxyImpl;
import com.infotech.isg.proxy.rightel.RightelProxyGetAccountBalanceResponse;

import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implementation for ISG balance service
 *
 * @author Sevak Gharibian
 */
@Service("ISGBalanceService")
public class ISGBalanceServiceImpl implements ISGBalanceService {
    private static final Logger LOG = LoggerFactory.getLogger(ISGBalanceServiceImpl.class);
    private static final Logger AUDITLOG = LoggerFactory.getLogger("isgdaemon.audit");

    private final BalanceRepository balanceRepository;
    private final BalanceLogRepository balanceLogRepository;

    private static final int MCI10000 = 10000;
    private static final int MCI20000 = 20000;
    private static final int MCI50000 = 50000;
    private static final int MCI100000 = 100000;
    private static final int MCI200000 = 200000;
    private static final int MCI500000 = 500000;
    private static final int MCI1000000 = 1000000;
    private List<Integer> cardAmounts = Arrays.asList(MCI10000, MCI20000, MCI50000, MCI100000, MCI200000, MCI500000, MCI1000000);

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

    @Value("${jiring.url}")
    private String jiringUrl;

    @Value("${jiring.username}")
    private String jiringUsername;

    @Value("${jiring.password}")
    private String jiringPassword;

    @Value("${rightel.url}")
    private String rightelUrl;

    @Value("${rightel.username}")
    private String rightelUsername;

    @Value("${rightel.password}")
    private String rightelPassword;

    @Value("${rightel.namespace}")
    private String rightelNamespace;

    @Autowired
    public ISGBalanceServiceImpl(BalanceRepository balanceRepository, BalanceLogRepository balanceLogRepository) {
        this.balanceRepository = balanceRepository;
        this.balanceLogRepository = balanceLogRepository;
    }

    private Long getMCIBalance(int amount) {

        MCIProxy mciProxy = new MCIProxyImpl(mciUrl, mciUsername, mciPassword, mciNamespace);

        try {
            MCIProxyGetTokenResponse tokenResponse = mciProxy.getToken();
            if ((tokenResponse == null)
                || (tokenResponse.getToken() == null)) {
                LOG.error("invalid GetToken response from MCI for balance, try again");
                return null;
            }

            String token = tokenResponse.getToken();
            MCIProxyGetRemainedBrokerRechargeResponse balanceResponse = mciProxy.getRemainedBrokerRecharge(token, amount);
            if ((balanceResponse == null)
                || (balanceResponse.getResponse() == null)
                || (balanceResponse.getResponse().size() < 2)
                || (balanceResponse.getCode() == null)
                || (balanceResponse.getDetail() == null)) {
                LOG.error("invalid GetRemainedBrokerRecharge response from MCI, try again");
                return null;
            }

            if (!balanceResponse.getCode().equals("0")) {
                LOG.debug("MCI responds error({}) for getRemainedBrokerRecharge({})", balanceResponse.getCode(), amount);
                return null;
            }

            return Long.valueOf(balanceResponse.getDetail());
        } catch (ProxyAccessException e) {
            LOG.error("error to get MCI balance, try again", e);
            return null;
        }
    }

    @Override
    @Transactional
    public void getMCIBalance() {
        BalanceLog balanceLog = new BalanceLog();
        for (Integer amount : cardAmounts) {
            Long balance = getMCIBalance(amount);
            AUDITLOG.info("MCI remained balance for {}: {}", amount, balance);
            if (balance != null) {
                switch (amount) {
                    case MCI10000:
                        balanceRepository.updateMCI10000(balance.longValue(), new Date());
                        balanceLog.setMci10000(balance);
                        balanceLog.setMci10000Timestamp(new Date());
                        break;

                    case MCI20000:
                        balanceRepository.updateMCI20000(balance.longValue(), new Date());
                        balanceLog.setMci20000(balance);
                        balanceLog.setMci20000Timestamp(new Date());
                       break;

                    case MCI50000:
                        balanceRepository.updateMCI50000(balance.longValue(), new Date());
                        balanceLog.setMci50000(balance);
                        balanceLog.setMci50000Timestamp(new Date());
                       break;

                    case MCI100000:
                        balanceRepository.updateMCI100000(balance.longValue(), new Date());
                        balanceLog.setMci100000(balance);
                        balanceLog.setMci100000Timestamp(new Date());
                       break;

                    case MCI200000:
                        balanceRepository.updateMCI200000(balance.longValue(), new Date());
                        balanceLog.setMci200000(balance);
                        balanceLog.setMci200000Timestamp(new Date());
                       break;

                    case MCI500000:
                        balanceRepository.updateMCI500000(balance.longValue(), new Date());
                        balanceLog.setMci500000(balance);
                        balanceLog.setMci500000Timestamp(new Date());
                       break;

                    case MCI1000000:
                        balanceRepository.updateMCI1000000(balance.longValue(), new Date());
                        balanceLog.setMci1000000(balance);
                        balanceLog.setMci1000000Timestamp(new Date());
                       break;

                    default: break;
                }
            }
        }
        balanceLogRepository.save(balanceLog);
    }

    @Override
    @Transactional
    public void getMTNBalance() {
        MTNProxy mtnProxy = new MTNProxyImpl(mtnUrl, mtnUsername, mtnPassword, mtnVendor, mtnNamespace);

        try {
            MTNProxyResponse response = mtnProxy.getBalance();
            if ((response.getResultCode() == null)
                || (response.getOrigResponseMessage() == null)) {
                LOG.error("invalid get balance response from MTN, try again");
                AUDITLOG.info("MTN get balance failed");
                return;
            }

            if (!response.getResultCode().equals("0")) {
                LOG.debug("MTN responds error({}) for get balance", response.getResultCode());
                AUDITLOG.info("MTN get balance failed");
                return;
            }

            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher m = pattern.matcher(response.getOrigResponseMessage());
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                sb.append(m.group());
            }

            if (sb.toString().isEmpty()) {
                LOG.error("MTN invalid get balance response: {}", response.getOrigResponseMessage());
                AUDITLOG.info("MTN get balance failed");
                return;
            }

            long balance = Long.parseLong(sb.toString());
            balanceRepository.updateMTN(balance, new Date());
            AUDITLOG.info("MTN get balance: {}", balance);

            BalanceLog balanceLog = new BalanceLog();
            balanceLog.setMtn(balance);
            balanceLog.setMtnTimestamp(new Date());
            balanceLogRepository.save(balanceLog);
        } catch (ProxyAccessException e) {
            LOG.error("error to get MTN balance, try again", e);
            AUDITLOG.info("MTN get balance failed");
        }
    }

    @Override
    @Transactional
    public void getJiringBalance() {
        JiringProxy jiringProxy = new JiringProxyImpl(jiringUrl, jiringUsername, jiringPassword);
        try {
            TCSResponse response = jiringProxy.balance();

            if ((response == null)
                || (response.getResult() == null)
                || (response.getMessage() == null)) {
                LOG.error("invalid get balance response from Jiring, try again");
                AUDITLOG.info("Jiring get balance failed");
                return;
            }

            if (!response.getResult().equals("0")) {
                LOG.debug("Jiring responds error({}) for get balance", response.getResult());
                AUDITLOG.info("Jiring get balance failed");
                return;
            }

            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher m = pattern.matcher(response.getMessage());
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                sb.append(m.group());
            }

            if (sb.toString().isEmpty()) {
                LOG.error("Jiring invalid get balance response: {}", response.getMessage());
                AUDITLOG.info("Jiring get balance failed");
                return;
            }

            long balance = Long.parseLong(sb.toString());
            balanceRepository.updateJiring(balance, new Date());
            AUDITLOG.info("Jiring get balance: {}", sb.toString());

            BalanceLog balanceLog = new BalanceLog();
            balanceLog.setJiring(balance);
            balanceLog.setJiringTimestamp(new Date());
            balanceLogRepository.save(balanceLog);
       } catch (ProxyAccessException e) {
            LOG.error("error to get balance from Jiring", e);
            AUDITLOG.info("Jiring get balance failed");
        }
    }

    @Override
    @Transactional
    public void getRightelBalance() {
        RightelProxy rightelProxy = new RightelProxyImpl(rightelUrl, rightelUsername, rightelPassword, rightelNamespace);

        try {
            RightelProxyGetAccountBalanceResponse response = rightelProxy.getAccountBalance();

            if (response.getErrorCode() != 0) {
                LOG.debug("Rightel responds error({}) for get balance", response.getErrorCode());
                AUDITLOG.info("Rightel get balance failed");
                return;
            }

            long balance = Long.parseLong(response.getValue());
            balanceRepository.updateRightel(balance, new Date());
            AUDITLOG.info("Rightel get balance: {}", response.getValue());

            BalanceLog balanceLog = new BalanceLog();
            balanceLog.setRightel(balance);
            balanceLog.setRightelTimestamp(new Date());
            balanceLogRepository.save(balanceLog);
        } catch (ProxyAccessException e) {
            LOG.error("error to get Rightel balance, try again", e);
            AUDITLOG.info("Rightel get balance failed");
        }
    }
}

