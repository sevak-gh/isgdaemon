package com.infotech.isg.service.impl;

import com.infotech.isg.domain.OperatorStatus;
import com.infotech.isg.domain.Operator;
import com.infotech.isg.service.ISGOperatorStatusService;
import com.infotech.isg.service.ISGException;
import com.infotech.isg.repository.OperatorStatusRepository;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.ProxyAccessException;
import com.infotech.isg.proxy.mci.MCIProxyImpl;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
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

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implementation for ISG operator status service
 *
 * @author Sevak Gharibian
 */
@Service("ISGOperatorStatusService")
public class ISGOperatorStatusServiceImpl implements ISGOperatorStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(ISGOperatorStatusServiceImpl.class);
    private static final Logger AUDITLOG = LoggerFactory.getLogger("isgdaemon.audit");

    private final OperatorStatusRepository operatorStatusRepository;

    @Value("${mci1.url}")
    private String mciUrl;

    @Value("${mci1.username}")
    private String mciUsername;

    @Value("${mci1.password}")
    private String mciPassword;

    @Value("${mci1.namespace}")
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
    public ISGOperatorStatusServiceImpl(OperatorStatusRepository operatorStatusRepository) {
        this.operatorStatusRepository = operatorStatusRepository;
    }

    @Override
    @Transactional
    public void getMCIStatus() {
        MCIProxy mciProxy = new MCIProxyImpl(mciUrl, mciUsername, mciPassword, mciNamespace);
        MCIProxyGetTokenResponse response = null;
        try {
            response = mciProxy.getToken();
        } catch (ProxyAccessException e) {
            LOG.error("error to get Token from MCI, operator status DOWN", e);
        }

        OperatorStatus operatorStatus = new OperatorStatus();
        operatorStatus.setId(Operator.MCI_ID);
        operatorStatus.setTimestamp(new Date());

        if ((response == null)
            || (response.getToken() == null)) {
            operatorStatus.setIsAvailable(false);
        } else {
            operatorStatus.setIsAvailable(true);
        }

        operatorStatusRepository.save(operatorStatus);
        AUDITLOG.info("MCI status: {}", operatorStatus.getIsAvailable());
    }

    @Override
    @Transactional
    public void getMTNStatus() {
        MTNProxy mtnProxy = new MTNProxyImpl(mtnUrl, mtnUsername, mtnPassword, mtnVendor, mtnNamespace);
        MTNProxyResponse response = null;
        try {
            response = mtnProxy.getBalance();
        } catch (ProxyAccessException e) {
            LOG.error("error to get balance from MTN, operator status DOWN", e);
        }

        OperatorStatus operatorStatus = new OperatorStatus();
        operatorStatus.setId(Operator.MTN_ID);
        operatorStatus.setTimestamp(new Date());

        if ((response == null)
            || (response.getResultCode() == null)) {
            operatorStatus.setIsAvailable(false);
        } else {
            operatorStatus.setIsAvailable(true);
        }

        operatorStatusRepository.save(operatorStatus);
        AUDITLOG.info("MTN status: {}", operatorStatus.getIsAvailable());
    }

    @Override
    @Transactional
    public void getJiringStatus() {
        JiringProxy jiringProxy = new JiringProxyImpl(jiringUrl, jiringUsername, jiringPassword);
        TCSResponse response = null;
        try {
            response = jiringProxy.balance();
        } catch (ProxyAccessException e) {
            LOG.error("error to get balance from Jiring, operator status DOWN", e);
        }

        OperatorStatus operatorStatus = new OperatorStatus();
        operatorStatus.setId(Operator.JIRING_ID);
        operatorStatus.setTimestamp(new Date());

        if ((response == null)
            || (response.getResult() == null)) {
            operatorStatus.setIsAvailable(false);
        } else {
            operatorStatus.setIsAvailable(true);
        }

        operatorStatusRepository.save(operatorStatus);
        AUDITLOG.info("Jiring status: {}", operatorStatus.getIsAvailable());
    }

    @Override
    @Transactional
    public void getRightelStatus() {
        RightelProxy rightelProxy = new RightelProxyImpl(rightelUrl, rightelUsername, rightelPassword, rightelNamespace);
        RightelProxyGetAccountBalanceResponse response = null;
        try {
            response = rightelProxy.getAccountBalance();
        } catch (ProxyAccessException e) {
            LOG.error("error to get balance from Rightel, operator status DOWN", e);
        }

        OperatorStatus operatorStatus = new OperatorStatus();
        operatorStatus.setId(Operator.RIGHTEL_ID);
        operatorStatus.setTimestamp(new Date());

        if ((response != null) && (response.getErrorCode() == 0)) { 
            operatorStatus.setIsAvailable(true);
        } else {
            operatorStatus.setIsAvailable(false);
        }

        operatorStatusRepository.save(operatorStatus);
        AUDITLOG.info("Rightel status: {}", operatorStatus.getIsAvailable());
    }
}
