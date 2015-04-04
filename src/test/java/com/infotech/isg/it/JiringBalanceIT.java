package com.infotech.com.it;

import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.proxy.jiring.JiringProxy;
import com.infotech.isg.proxy.jiring.TCSResponse;
import com.infotech.isg.it.fake.jiring.JiringFake;
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
 * integration test for Jiring Balance service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class JiringBalanceIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(JiringBalanceIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake jiring web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    JiringFake jiringFake;

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
    }

    @AfterMethod
    public void tearDown() {
        jiringFake.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        String jiringResponseCode = "0";
        long jiringBalance = 519676300L;
        String jiringResponseMessage = "your jiring balance is 519,676,300 IRR.";
        JiringProxy jiringProxy = new JiringProxy() {
            @Override
            public TCSResponse salesRequest(String consumer, int amount, String brandId) {
                throw new UnsupportedOperationException("jiring salesRequest not implemented");
            }

            @Override
            public TCSResponse salesRequestExec(String param, boolean checkOnly) {
                throw new UnsupportedOperationException("jiring salesRequestExec not implemented");
            }

            @Override
            public TCSResponse balance() {
                TCSResponse response = new TCSResponse();
                response.setResult(jiringResponseCode);
                response.setMessage(jiringResponseMessage);
                return response;
            }
        };
        jiringFake.setJiringProxyImpl(jiringProxy);
        jiringFake.start();

        // act
        isgBalanceService.getJiringBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select Jiring from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(jiringBalance));
    }

    @Test
    public void shouldNotCrashAndNotUpdateWhenInvalidResopnse() {
        // arrange
        String jiringResponseCode = "0";
        long jiringBalance = 519676300L;
        String jiringResponseMessage = "your jiring balance is jingul IRR.";
        JiringProxy jiringProxy = new JiringProxy() {
            @Override
            public TCSResponse salesRequest(String consumer, int amount, String brandId) {
                throw new UnsupportedOperationException("jiring salesRequest not implemented");
            }

            @Override
            public TCSResponse salesRequestExec(String param, boolean checkOnly) {
                throw new UnsupportedOperationException("jiring salesRequestExec not implemented");
            }

            @Override
            public TCSResponse balance() {
                TCSResponse response = new TCSResponse();
                response.setResult(jiringResponseCode);
                response.setMessage(jiringResponseMessage);
                return response;
            }
        };
        jiringFake.setJiringProxyImpl(jiringProxy);
        jiringFake.start();

        // act
        isgBalanceService.getJiringBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select Jiring from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(0L));    // means not updated
    }
}
