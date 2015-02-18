package com.infotech.com.it;

import com.infotech.isg.domain.Transaction;
import com.infotech.isg.domain.Operator;
import com.infotech.isg.repository.TransactionRepository;
import com.infotech.isg.proxy.mtn.MTNProxy;
import com.infotech.isg.proxy.mtn.MTNProxyResponse;
import com.infotech.isg.it.fake.mtn.MTNWSFake;
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
 * integration test for MTN verify service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MTNVerifyIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(MTNVerifyIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake mtn web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    MTNWSFake mtnws;

    @Autowired
    ISGVerifyService isgVerifyService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_transactions");
    }

    @AfterMethod
    public void tearDown() {
        mtnws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        jdbcTemplate.update("insert into info_topup_transactions ("
                            + "provider,type,state,resnum,refnum,clientip,"
                            + "amount,channel,consumer,bankcode,client,customerip,"
                            + "trtime,status,operator,stf,stfresult"
                            + ") values("
                            + "1,1,'state','res','ref','1.1.1.1',20000,59,'09365067064','054',1,'2.2.2.2',now(),-1,1,1,0"
                            + ")");
        String mtnTransactionId = "T123456789";
        String mtnOrigResponseMessage = "TID: 123456 BANK_TID: 321333 CHECK: 1 PROCESS: 1 NOTIFY: 1 ";
        String mtnCommandStatus = "OK";
        String mtnResultCode = "0";
        MTNProxy mtnService = new MTNProxy() {
            @Override
            public MTNProxyResponse recharge(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("recharge not supported");
            }

            @Override
            public MTNProxyResponse billPayment(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("bill payment not supported");
            }

            @Override
            public MTNProxyResponse bulkTransfer(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("bulk transafer not supported");
            }

            @Override
            public MTNProxyResponse wow(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("wow not supported");
            }

            @Override
            public MTNProxyResponse postPaidWimax(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("post wimax not supported");
            }

            @Override
            public MTNProxyResponse prePaidWimax(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("pre wimax not supported");
            }

            @Override
            public MTNProxyResponse gprs(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("gprs not supported");
            }

            @Override
            public MTNProxyResponse verify(long trId) {
                MTNProxyResponse response = new MTNProxyResponse();
                response.setTransactionId(mtnTransactionId);
                response.setOrigResponseMessage(mtnOrigResponseMessage);
                response.setCommandStatus(mtnCommandStatus);
                response.setResultCode(mtnResultCode);
                return response;
            }

            @Override
            public MTNProxyResponse getBalance() {
                throw new UnsupportedOperationException("balance not supported");
            }
        };
        mtnws.setServiceImpl(mtnService);
        mtnws.publish();

        // act
        isgVerifyService.mtnVerify();

        // assert
        Integer stf = jdbcTemplate.queryForObject("select stf from info_topup_transactions", Integer.class);
        assertThat(stf, is(notNullValue()));
        assertThat(stf.intValue(), is(2));
        String operatorResponse = jdbcTemplate.queryForObject("select oprresponse from info_topup_transactions", String.class);
        assertThat(operatorResponse, is(notNullValue()));
        assertThat(operatorResponse, is(mtnOrigResponseMessage));
        String serial = jdbcTemplate.queryForObject("select oprtid from info_topup_transactions", String.class);
        assertThat(serial, is(notNullValue()));
        assertThat(serial, is(mtnTransactionId));
    }

    @Test
    public void shouldSetStfFailedWhenVerifyProcessNOK() {
        // arrange
        jdbcTemplate.update("insert into info_topup_transactions ("
                            + "provider,type,state,resnum,refnum,clientip,"
                            + "amount,channel,consumer,bankcode,client,customerip,"
                            + "trtime,status,operator,stf,stfresult"
                            + ") values("
                            + "1,1,'state','res','ref','1.1.1.1',20000,59,'09365067064','054',1,'2.2.2.2',now(),-1,1,1,0"
                            + ")");
        String mtnTransactionId = "T123456789";
        String mtnOrigResponseMessage = "TID: 123456 BANK_TID: 321333 CHECK: 1 PROCESS: 23 NOTIFY: 1 ";
        String mtnCommandStatus = "OK";
        String mtnResultCode = "0";
        MTNProxy mtnService = new MTNProxy() {
            @Override
            public MTNProxyResponse recharge(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("recharge not supported");
            }

            @Override
            public MTNProxyResponse billPayment(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("bill payment not supported");
            }

            @Override
            public MTNProxyResponse bulkTransfer(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("bulk transafer not supported");
            }

            @Override
            public MTNProxyResponse wow(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("wow not supported");
            }

            @Override
            public MTNProxyResponse postPaidWimax(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("post wimax not supported");
            }

            @Override
            public MTNProxyResponse prePaidWimax(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("pre wimax not supported");
            }

            @Override
            public MTNProxyResponse gprs(String consumer, int amount, long trId) {
                throw new UnsupportedOperationException("gprs not supported");
            }

            @Override
            public MTNProxyResponse verify(long trId) {
                MTNProxyResponse response = new MTNProxyResponse();
                response.setTransactionId(mtnTransactionId);
                response.setOrigResponseMessage(mtnOrigResponseMessage);
                response.setCommandStatus(mtnCommandStatus);
                response.setResultCode(mtnResultCode);
                return response;
            }

            @Override
            public MTNProxyResponse getBalance() {
                throw new UnsupportedOperationException("balance not supported");
            }
        };
        mtnws.setServiceImpl(mtnService);
        mtnws.publish();

        // act
        isgVerifyService.mtnVerify();

        // assert
        Integer stf = jdbcTemplate.queryForObject("select stf from info_topup_transactions", Integer.class);
        assertThat(stf, is(notNullValue()));
        assertThat(stf.intValue(), is(3));
        String operatorResponse = jdbcTemplate.queryForObject("select oprresponse from info_topup_transactions", String.class);
        assertThat(operatorResponse, is(notNullValue()));
        assertThat(operatorResponse, is(mtnOrigResponseMessage));
        String serial = jdbcTemplate.queryForObject("select oprtid from info_topup_transactions", String.class);
        assertThat(serial, is(notNullValue()));
        assertThat(serial, is(mtnTransactionId));
    }
}
