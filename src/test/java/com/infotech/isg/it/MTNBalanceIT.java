package com.infotech.com.it;

import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.proxy.mtn.MTNProxy;
import com.infotech.isg.proxy.mtn.MTNProxyResponse;
import com.infotech.isg.it.fake.mtn.MTNWSFake;
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
 * integration test for MTN Balance service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MTNBalanceIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(MTNBalanceIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake mtn web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    MTNWSFake mtnws;

    @Autowired
    ISGBalanceService isgBalanceService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("update info_topup_balance set "
                            + "MCI10000 = 0, MCI10000Timestamp = now(),"
                            + "MCI20000 = 0, MCI20000Timestamp = now(),"
                            + "MCI50000 = 0, MCI50000Timestamp = now(),"
                            + "MCI100000 = 0, MCI100000Timestamp = now(),"
                            + "MCI200000 = 0 ,MCI200000Timestamp = now(),"
                            + "MCI500000 = 0, MCI500000Timestamp = now(),"
                            + "MCI1000000 = 0, MCI1000000Timestamp = now(),"
                            + "MTN = 0, MTNTimestamp = now(),"
                            + "Jiring = 0, JiringTimestamp = now()");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_balance_log");
    }

    @AfterMethod
    public void tearDown() {
        mtnws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        long mtnBalance = 12346549;
        String mtnTransactionId = null;
        String mtnOrigResponseMessage = String.format("your current account balance is %d Rls.", mtnBalance);
        String mtnCommandStatus = "OK";
        String mtnResultCode = "0";
        MTNProxy mtnService = new MTNProxy() {
            @Override
            public MTNProxyResponse recharge(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("recharge not supported");
            }

            @Override
            public MTNProxyResponse billPayment(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("bill payment not supported");
            }

            @Override
            public MTNProxyResponse bulkTransfer(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("bulk transafer not supported");
            }

            @Override
            public MTNProxyResponse wow(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("wow not supported");
            }

            @Override
            public MTNProxyResponse postPaidWimax(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("post wimax not supported");
            }

            @Override
            public MTNProxyResponse prePaidWimax(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("pre wimax not supported");
            }

            @Override
            public MTNProxyResponse gprs(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("gprs not supported");
            }

            @Override
            public MTNProxyResponse gprsCombo(String consumer, int amount, long trId, int profileId, String channel) {
                throw new UnsupportedOperationException("gprs combo not supported");
            }

            @Override
            public MTNProxyResponse verify(long trId) {
                throw new UnsupportedOperationException("verify not supported");
            }

            @Override
            public MTNProxyResponse getBalance() {
                MTNProxyResponse response = new MTNProxyResponse();
                response.setTransactionId(mtnTransactionId);
                response.setOrigResponseMessage(mtnOrigResponseMessage);
                response.setCommandStatus(mtnCommandStatus);
                response.setResultCode(mtnResultCode);
                return response;
            }
        };
        mtnws.setServiceImpl(mtnService);
        mtnws.publish();

        // act
        isgBalanceService.getMTNBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select MTN from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(mtnBalance));
    }

    @Test
    public void shouldNotCrashAndNotUpdateWhenInvalidResopnse() {
        // arrange
        long mtnBalance = 12346549;
        String mtnTransactionId = null;
        String mtnOrigResponseMessage = String.format("your current account balance is i don't know Rls.");
        String mtnCommandStatus = "OK";
        String mtnResultCode = "0";
        MTNProxy mtnService = new MTNProxy() {
            @Override
            public MTNProxyResponse recharge(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("recharge not supported");
            }

            @Override
            public MTNProxyResponse billPayment(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("bill payment not supported");
            }

            @Override
            public MTNProxyResponse bulkTransfer(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("bulk transafer not supported");
            }

            @Override
            public MTNProxyResponse wow(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("wow not supported");
            }

            @Override
            public MTNProxyResponse postPaidWimax(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("post wimax not supported");
            }

            @Override
            public MTNProxyResponse prePaidWimax(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("pre wimax not supported");
            }

            @Override
            public MTNProxyResponse gprs(String consumer, int amount, long trId, String channel) {
                throw new UnsupportedOperationException("gprs not supported");
            }

            @Override
            public MTNProxyResponse gprsCombo(String consumer, int amount, long trId, int profileId, String channel) {
                throw new UnsupportedOperationException("gprs combo not supported");
            }

            @Override
            public MTNProxyResponse verify(long trId) {
                throw new UnsupportedOperationException("verify not supported");
            }

            @Override
            public MTNProxyResponse getBalance() {
                MTNProxyResponse response = new MTNProxyResponse();
                response.setTransactionId(mtnTransactionId);
                response.setOrigResponseMessage(mtnOrigResponseMessage);
                response.setCommandStatus(mtnCommandStatus);
                response.setResultCode(mtnResultCode);
                return response;
            }
        };
        mtnws.setServiceImpl(mtnService);
        mtnws.publish();

        // act
        isgBalanceService.getMTNBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select MTN from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(0L));
    }
}
