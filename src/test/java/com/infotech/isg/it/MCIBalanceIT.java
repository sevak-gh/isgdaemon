package com.infotech.com.it;

import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyRechargeResponse;
import com.infotech.isg.proxy.mci.MCIProxyRechargeVerifyResponse;
import com.infotech.isg.proxy.mci.MCIProxyGetRemainedBrokerRechargeResponse;
import com.infotech.isg.it.fake.mci.MCIWSFake;
import com.infotech.isg.service.ISGBalanceService;

import javax.sql.DataSource;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * integration test for MCI Balance service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MCIBalanceIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(MCIBalanceIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake mci web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    MCIWSFake mciws;

    @Autowired
    ISGBalanceService isgBalanceService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_balance");
        jdbcTemplate.update("insert into info_topup_balance ("
                            + "MCI10000,MCI10000Timestamp,"
                            + "MCI20000,MCI20000Timestamp,"
                            + "MCI50000,MCI50000Timestamp,"
                            + "MCI100000,MCI100000Timestamp,"
                            + "MCI200000,MCI200000Timestamp,"
                            + "MCI500000,MCI500000Timestamp,"
                            + "MCI1000000,MCI1000000Timestamp,"
                            + "MTN,MTNTimestamp,"
                            + "Jiring,JiringTimestamp"
                            + ") values("
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now(),"
                            + "0, now()"
                            + ")");
    }

    @AfterMethod
    public void tearDown() {
        mciws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        String token = "token";
        String mciResponseCode = "0";
        String mciBalance = "1231231230";
        MCIProxy mciService = new MCIProxy() {
            @Override
            public MCIProxyGetTokenResponse getToken() {
                MCIProxyGetTokenResponse response = new MCIProxyGetTokenResponse();
                response.setToken(token);
                return response;
            }

            @Override
            public MCIProxyGetRemainedBrokerRechargeResponse getRemainedBrokerRecharge(String token, int amount) {
                List<String> response = new ArrayList<String>();
                response.add(mciResponseCode);      // response code
                response.add(mciBalance);           // response detail: balance
                MCIProxyGetRemainedBrokerRechargeResponse balanceResponse = new MCIProxyGetRemainedBrokerRechargeResponse();
                balanceResponse.setResponse(response);
                return balanceResponse;
            }

            @Override
            public MCIProxyRechargeResponse recharge(String token, String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("MCI recharge verify not implemented");
            }

            @Override
            public MCIProxyRechargeVerifyResponse rechargeVerify(String token, String consumer, long trId) {
                throw new UnsupportedOperationException("MCI recharge verify not implemented");
            }
        };
        mciws.setServiceImpl(mciService);
        mciws.publish();

        // act
        isgBalanceService.getMCIBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI10000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI20000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI50000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI100000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI200000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI500000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
        balance = jdbcTemplate.queryForObject("select MCI1000000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(Long.parseLong(mciBalance)));
    }

    @Test
    public void shouldNotCrashAndNotUpdateBalanceIfServiceProviderNotAvailable() {
        // arrange
        // MCI fake WS not published

        // act
        isgBalanceService.getMCIBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI10000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI20000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI50000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI100000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI200000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI500000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI1000000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
    }

    @Test
    public void shouldNotCrashAndNotUpdateBalanceIfServiceProviderThrowsException() {
        // arrange
        String token = "token";
        String mciResponseCode = "0";
        String mciBalance = "1231231230";
        MCIProxy mciService = new MCIProxy() {
            @Override
            public MCIProxyGetTokenResponse getToken() {
                MCIProxyGetTokenResponse response = new MCIProxyGetTokenResponse();
                response.setToken(token);
                return response;
            }

            @Override
            public MCIProxyGetRemainedBrokerRechargeResponse getRemainedBrokerRecharge(String token, int amount) {
                throw new RuntimeException("something terrible happened!!!");
            }
 
            @Override
            public MCIProxyRechargeResponse recharge(String token, String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("MCI recharge verify not implemented");
            }

            @Override
            public MCIProxyRechargeVerifyResponse rechargeVerify(String token, String consumer, long trId) {
                throw new UnsupportedOperationException("MCI recharge verify not implemented");
            }
        };
        mciws.setServiceImpl(mciService);
        mciws.publish();

        // act
        isgBalanceService.getMCIBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI10000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI20000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI50000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI100000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI200000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI500000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
        balance = jdbcTemplate.queryForObject("select MCI1000000 from info_topup_balance", Long.class);
        assertThat(balance.longValue(), is(0L));
    }
}
