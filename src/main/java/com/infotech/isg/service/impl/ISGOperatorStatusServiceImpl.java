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

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ISGOperatorStatusServiceImpl(@Qualifier("JdbcOperatorStatusRepository") OperatorStatusRepository operatorStatusRepository) {
        this.operatorStatusRepository = operatorStatusRepository;
    }

    @Override
    public void getMCIStatus() {
        MCIProxy mciProxy = new MCIProxyImpl(mciUrl, mciUsername, mciPassword, mciNamespace);
        MCIProxyGetTokenResponse response = null;
        try {
            response = mciProxy.getToken();
        } catch (ProxyAccessException e) {
            LOG.error("unable to get Token from MCI, operator status DOWN", e);
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

        operatorStatusRepository.update(operatorStatus);
    }

    @Override
    public void getMTNStatus() {
        MTNProxy mtnProxy = new MTNProxyImpl(mtnUrl, mtnUsername, mtnPassword, mtnVendor, mtnNamespace);
        MTNProxyResponse response = null;
        try {
            response = mtnProxy.getBalance();
        } catch (ProxyAccessException e) {
            LOG.error("unable to get balance from MTN, operator status DOWN", e);
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

        operatorStatusRepository.update(operatorStatus);
    }

    @Override
    public void getJiringStatus() {
        //TODO: to be implemented
    }
}
