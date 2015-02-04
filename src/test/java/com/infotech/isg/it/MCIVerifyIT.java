package com.infotech.com.it;

import com.infotech.isg.domain.Transaction;
import com.infotech.isg.domain.Operator;
import com.infotech.isg.repository.TransactionRepository;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyRechargeVerifyResponse;
import com.infotech.isg.proxy.mci.MCIProxyRechargeResponse;
import com.infotech.isg.proxy.mci.MCIProxyGetRemainedBrokerRechargeResponse;
import com.infotech.isg.it.fake.mci.MCIWSFake;
import com.infotech.isg.service.ISGVerifyService;

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
 * integration test for MCI verify service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MCIVerifyIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(MCIVerifyIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake mci web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    MCIWSFake mciws;

    @Autowired
    ISGVerifyService isgVerifyService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_transactions");
    }

    @AfterMethod
    public void tearDown() {
        mciws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        jdbcTemplate.update("insert into info_topup_transactions ("
                            + "provider,type,state,resnum,refnum,clientip,"
                            + "amount,channel,consumer,bankcode,client,customerip,"
                            + "trtime,status,operator,stf,stfresult"
                            + ") values("
                            + "2,1,'state','res','ref','1.1.1.1',20000,59,'09125067064','054',1,'2.2.2.2',now(),-1,2,1,0"
                            + ")");
        String token = "token";
        String mciVerifyResponseCode = "0";
        String mciVerifyResponseDetail = "recharge verify done";
        String mciResponseCode = "0";
        String mciResponseDetail = "recharge done";
        String mciResponseDate = "2016-02-04";
        String mciResponseTime = "12:00:00";
        String mciResponseSerial = "112233445566";
        MCIProxy mciService = new MCIProxy() {
            @Override
            public MCIProxyGetTokenResponse getToken() {
                MCIProxyGetTokenResponse response = new MCIProxyGetTokenResponse();
                response.setToken(token);
                return response;
            }

            @Override
            public MCIProxyRechargeVerifyResponse rechargeVerify(String token, String consumer, long id) {
                List<String> response = new ArrayList<String>();
                response.add(mciVerifyResponseCode);        // verify response code
                response.add(mciVerifyResponseDetail);      // verify response detail
                response.add(mciResponseCode);              // response code
                response.add(mciResponseDetail);            // response detail: balance
                response.add(mciResponseDate);
                response.add(mciResponseTime);
                response.add(mciResponseSerial);
                MCIProxyRechargeVerifyResponse rechargeVerifyResponse = new MCIProxyRechargeVerifyResponse();
                rechargeVerifyResponse.setResponse(response);
                return rechargeVerifyResponse;
            }

            @Override
            public MCIProxyRechargeResponse recharge(String token, String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("MCI recharge verify not implemented");
            }

            @Override
            public MCIProxyGetRemainedBrokerRechargeResponse getRemainedBrokerRecharge(String token, int amount) {
                throw new UnsupportedOperationException("MCI remained broker not implemented");
            }
        };
        mciws.setServiceImpl(mciService);
        mciws.publish();

        // act
        isgVerifyService.mciVerify();

        // assert
        Integer stf = jdbcTemplate.queryForObject("select stf from info_topup_transactions", Integer.class);
        assertThat(stf, is(notNullValue()));
        assertThat(stf.intValue(), is(2));
        String operatorResponse = jdbcTemplate.queryForObject("select oprresponse from info_topup_transactions", String.class);
        assertThat(operatorResponse, is(notNullValue()));
        assertThat(operatorResponse, is(mciResponseDetail));
        String serial = jdbcTemplate.queryForObject("select oprtid from info_topup_transactions", String.class);
        assertThat(serial, is(notNullValue()));
        assertThat(serial, is(mciResponseSerial));
    }
}
