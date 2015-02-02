package com.infotech.isg.service.impl;

import com.infotech.isg.service.ISGBalanceService;
import com.infotech.isg.service.ISGException;
import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyImpl;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyGetRemainedBrokerRechargeResponse;

import java.util.List;
import java.util.Arrays;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final BalanceRepository balanceRepository;

    private static final int MCI10000 = 10000;
    private static final int MCI20000 = 20000;
    private static final int MCI50000 = 50000;
    private static final int MCI100000 = 100000;
    private static final int MCI200000 = 200000;
    private static final int MCI500000 = 500000;
    private static final int MCI1000000 = 1000000;
    private List<Integer> cardAmounts = Arrays.asList(MCI10000, MCI20000, MCI50000, MCI100000, MCI200000, MCI500000, MCI1000000);

    @Value("${mci.url}")
    private String url;

    @Value("${mci.username}")
    private String username;

    @Value("${mci.password}")
    private String password;

    @Value("${mci.namespace}")
    private String namespace;

    @Autowired
    public ISGBalanceServiceImpl(@Qualifier("JdbcBalanceRepository") BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    private Long getMCIBalance(int amount) {
        
        MCIProxy mciProxy = new MCIProxyImpl(url, username, password, namespace);

        MCIProxyGetTokenResponse tokenResponse = mciProxy.getToken();
        if ((tokenResponse == null)
            || (tokenResponse.getToken() == null)) {
            throw new ISGException("invalid GetToken response");
        }

        String token = tokenResponse.getToken();
        MCIProxyGetRemainedBrokerRechargeResponse balanceResponse = mciProxy.getRemainedBrokerRecharge(token, amount);
        if ((balanceResponse == null)
            || (balanceResponse.getResponse() == null)
            || (balanceResponse.getResponse().size() < 2)
            || (balanceResponse.getCode() == null)
            || (balanceResponse.getDetail() == null)) {
            throw new ISGException("invalid GetRemainedBrokerRecharge response");
        }

        if (!balanceResponse.getCode().equalsIgnoreCase("0")) {
            LOG.error("operator responds error({}) for getRemainedBrokerRecharge({})", balanceResponse.getCode(), amount);
            return null;
        }

        return Long.valueOf(balanceResponse.getDetail());
    }

    @Override
    public void getMCIBalance() {
        for (Integer amount : cardAmounts) {
            Long balance = getMCIBalance(amount);
            LOG.info("MCI remained balance for {}: {}", amount, balance);
            if (balance != null) {
                switch (amount) {
                    case MCI10000:
                        balanceRepository.updateMCI10000(balance.longValue(), new Date());
                        break;

                    case MCI20000:
                        balanceRepository.updateMCI20000(balance.longValue(), new Date());
                        break;

                    case MCI50000:
                        balanceRepository.updateMCI50000(balance.longValue(), new Date());
                        break;

                    case MCI100000:
                        balanceRepository.updateMCI100000(balance.longValue(), new Date());
                        break;

                    case MCI200000:
                        balanceRepository.updateMCI200000(balance.longValue(), new Date());
                        break;

                    case MCI500000:
                        balanceRepository.updateMCI500000(balance.longValue(), new Date());
                        break;

                    case MCI1000000:
                        balanceRepository.updateMCI1000000(balance.longValue(), new Date());
                        break;

                    default: break;
                }
            }
        }
    }

    @Override
    public void getMTNBalance() {
        LOG.error("getMTNBalance method not impleneted");
    }

    @Override
    public void getJiringBalance() {
        LOG.error("getJiringBalance method not impleneted");
    }
}

