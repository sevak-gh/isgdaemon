package com.infotech.com.it;

import com.infotech.isg.repository.BalanceRepository;
import com.infotech.isg.proxy.rightel.RightelProxy;
import com.infotech.isg.proxy.rightel.RightelProxySubmitChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyConfirmChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyInquiryChargeResponse;
import com.infotech.isg.proxy.rightel.RightelProxyGetAccountBalanceResponse;
import com.infotech.isg.it.fake.rightel.RightelWSFake;
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
 * integration test for rightel service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class RightelBalanceIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(RightelBalanceIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake rightel web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    RightelWSFake rightelws;

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
                            + "Jiring = 0, JiringTimestamp = now(),"
                            + "Rightel = 0, RightelTimestamp = now()");
    }

    @AfterMethod
    public void tearDown() {
        rightelws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        int errorCode = 0;
        String errorDesc = "Success";
        String value = "123456789";
        RightelProxy rightelService = new RightelProxy() {
                       
            @Override
            public RightelProxySubmitChargeRequestResponse submitChargeRequest(String consumer, int amount) {
                throw new UnsupportedOperationException("submit charge request not supported");
            }

            @Override
            public RightelProxyConfirmChargeRequestResponse confirmChargeRequest(String requestId, long trId) {
                throw new UnsupportedOperationException("confirm charge request not supported");
            }
    
            @Override
            public RightelProxyInquiryChargeResponse inquiryCharge(long trId) {
                throw new UnsupportedOperationException("charge inquiery not supported");
            }

            @Override
            public RightelProxyGetAccountBalanceResponse getAccountBalance() {
                RightelProxyGetAccountBalanceResponse response = new RightelProxyGetAccountBalanceResponse();
                response.setErrorCode(errorCode);
                response.setErrorDesc(errorDesc);
                response.setValue(value);
                return response;
            }
        };
        rightelws.setServiceImpl(rightelService);
        rightelws.publish();

        // act
        isgBalanceService.getRightelBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select Rightel from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(Long.parseLong(value)));
    }

    @Test
    public void shouldNotCrashAndNotUpdateWhenInvalidResopnse() {
        // arrange
        int errorCode = 0;
        String errorDesc = "Success";
        String value = "this is invalid value";
        RightelProxy rightelService = new RightelProxy() {
                       
            @Override
            public RightelProxySubmitChargeRequestResponse submitChargeRequest(String consumer, int amount) {
                throw new UnsupportedOperationException("submit charge request not supported");
            }

            @Override
            public RightelProxyConfirmChargeRequestResponse confirmChargeRequest(String requestId, long trId) {
                throw new UnsupportedOperationException("confirm charge request not supported");
            }
    
            @Override
            public RightelProxyInquiryChargeResponse inquiryCharge(long trId) {
                throw new UnsupportedOperationException("charge inquiery not supported");
            }

            @Override
            public RightelProxyGetAccountBalanceResponse getAccountBalance() {
                RightelProxyGetAccountBalanceResponse response = new RightelProxyGetAccountBalanceResponse();
                response.setErrorCode(errorCode);
                response.setErrorDesc(errorDesc);
                response.setValue(value);
                return response;
            }
        };
        rightelws.setServiceImpl(rightelService);
        rightelws.publish();

        // act
        isgBalanceService.getRightelBalance();

        // assert
        Long balance = jdbcTemplate.queryForObject("select Rightel from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(0L));
    }
}
