package com.infotech.com.it;

import com.infotech.isg.domain.Transaction;
import com.infotech.isg.domain.Operator;
import com.infotech.isg.repository.TransactionRepository;
import com.infotech.isg.proxy.rightel.RightelProxy;
import com.infotech.isg.proxy.rightel.RightelProxySubmitChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyConfirmChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyInquiryChargeResponse;
import com.infotech.isg.proxy.rightel.RightelProxyGetAccountBalanceResponse;
import com.infotech.isg.it.fake.rightel.RightelWSFake;
import com.infotech.isg.service.ISGVerifyService;

import javax.sql.DataSource;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

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
 * integration test for Rightel verify service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class RightelVerifyIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(RightelVerifyIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake rightel web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    RightelWSFake rightelws;

    @Autowired
    ISGVerifyService isgVerifyService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_transactions");
    }

    @AfterMethod
    public void tearDown() {
        rightelws.stop();
    }

    @Test
    public void HappyPathShouldSucceed() {
        // arrange
        jdbcTemplate.update("insert into info_topup_transactions ("
                            + "provider,type,state,resnum,refnum,clientip,"
                            + "amount,channel,consumer,bankcode,client,customerip,"
                            + "trtime,status,operator,stf,stfresult"
                            + ") values("
                            + "4,1,'state','res','ref','1.1.1.1',20000,59,'09265067064','054',1,'2.2.2.2',now(),-1,1,1,0"
                            + ")");
        int errorCode = 0;
        String errorDesc = "Success";
        BigDecimal vat = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(3000);
        BigDecimal billAmount = new BigDecimal(47000);
        int billState = 1;
        String voucherSerial = "";
        String chargeResponse = "40500000";
        String chargeResponseDesc = "The operation done successfully";
        int status = 4;
        String statusTime = "3/27/2015 3:22:41 PM";
        String transactionId = "TRX123";
        String telNo = "09265067064";
        BigDecimal decimalAmount = new BigDecimal(10000);
        RightelProxy rightelService = new RightelProxy() {
                       
            @Override
            public RightelProxySubmitChargeRequestResponse submitChargeRequest(String consumer, int amount, int channel) {
                throw new UnsupportedOperationException("submit charge request not supported");
            }

            @Override
            public RightelProxyConfirmChargeRequestResponse confirmChargeRequest(String requestId, long trId) {
                throw new UnsupportedOperationException("confirm charge request not supported");
            }
    
            @Override
            public RightelProxyInquiryChargeResponse inquiryCharge(long trId) {
                RightelProxyInquiryChargeResponse response = new RightelProxyInquiryChargeResponse();
                response.setErrorCode(errorCode);
                response.setErrorDesc(errorDesc);
                response.setRequestId(transactionId);
                response.setTelNo(telNo); 
                response.setAmount(decimalAmount); 
                response.setVat(vat); 
                response.setDiscount(discount); 
                response.setBillAmount(billAmount);
                response.setBillState(billState);
                response.setVoucherSerial(voucherSerial);
                response.setChargeResponse(chargeResponse);
                response.setChargeResponseDesc(chargeResponseDesc);
                response.setVerifyResponse(chargeResponse);
                response.setVerifyResponseDesc(chargeResponseDesc);
                response.setTransactionId(String.format("Info%d", trId));
                response.setStatus(status);
                response.setStatusTime(statusTime);
                return response;
            }

            @Override
            public RightelProxyGetAccountBalanceResponse getAccountBalance() {
                throw new UnsupportedOperationException("get balance not supported");
            }
        };
        rightelws.setServiceImpl(rightelService);
        rightelws.publish();

        // act
        isgVerifyService.rightelVerify();

        // assert
        Integer stf = jdbcTemplate.queryForObject("select stf from info_topup_transactions", Integer.class);
        assertThat(stf, is(notNullValue()));
        assertThat(stf.intValue(), is(2));
        String operatorResponse = jdbcTemplate.queryForObject("select oprresponse from info_topup_transactions", String.class);
        assertThat(operatorResponse, is(notNullValue()));
        assertThat(operatorResponse, is(chargeResponseDesc));
        String serial = jdbcTemplate.queryForObject("select oprtid from info_topup_transactions", String.class);
        assertThat(serial, is(notNullValue()));
        assertThat(serial, is(transactionId));
    }

    @Test
    public void shouldSetStfFailedWhenVerifyProcessNOK() {
        // arrange
        jdbcTemplate.update("insert into info_topup_transactions ("
                            + "provider,type,state,resnum,refnum,clientip,"
                            + "amount,channel,consumer,bankcode,client,customerip,"
                            + "trtime,status,operator,stf,stfresult"
                            + ") values("
                            + "4,1,'state','res','ref','1.1.1.1',20000,59,'09265067064','054',1,'2.2.2.2',now(),-1,1,1,0"
                            + ")");
        int errorCode = 0;
        String errorDesc = "Success";
        BigDecimal vat = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(3000);
        BigDecimal billAmount = new BigDecimal(47000);
        int billState = 1;
        String voucherSerial = "";
        String chargeResponse = "40500000";
        String chargeResponseDesc = "The operation done successfully";
        int status = 6;
        String statusTime = "3/27/2015 3:22:41 PM";
        String transactionId = "TRX123";
        String telNo = "09265067064";
        BigDecimal decimalAmount = new BigDecimal(10000);
        RightelProxy rightelService = new RightelProxy() {
                       
            @Override
            public RightelProxySubmitChargeRequestResponse submitChargeRequest(String consumer, int amount, int channel) {
                throw new UnsupportedOperationException("submit charge request not supported");
            }

            @Override
            public RightelProxyConfirmChargeRequestResponse confirmChargeRequest(String requestId, long trId) {
                throw new UnsupportedOperationException("confirm charge request not supported");
            }
    
            @Override
            public RightelProxyInquiryChargeResponse inquiryCharge(long trId) {
                RightelProxyInquiryChargeResponse response = new RightelProxyInquiryChargeResponse();
                response.setErrorCode(errorCode);
                response.setErrorDesc(errorDesc);
                response.setRequestId(transactionId); 
                response.setTelNo(telNo); 
                response.setAmount(decimalAmount); 
                response.setVat(vat); 
                response.setDiscount(discount); 
                response.setBillAmount(billAmount);
                response.setBillState(billState);
                response.setVoucherSerial(voucherSerial);
                response.setChargeResponse(chargeResponse);
                response.setChargeResponseDesc(chargeResponseDesc);
                response.setVerifyResponse(chargeResponse);
                response.setVerifyResponseDesc(chargeResponseDesc);
                response.setTransactionId(String.format("Info%d", trId));
                response.setStatus(status);
                response.setStatusTime(statusTime);
                return response;
            }

            @Override
            public RightelProxyGetAccountBalanceResponse getAccountBalance() {
                throw new UnsupportedOperationException("get balance not supported");
            }
        };
        rightelws.setServiceImpl(rightelService);
        rightelws.publish();

        // act
        isgVerifyService.rightelVerify();

        // assert
        Integer stf = jdbcTemplate.queryForObject("select stf from info_topup_transactions", Integer.class);
        assertThat(stf, is(notNullValue()));
        assertThat(stf.intValue(), is(3));
        String operatorResponse = jdbcTemplate.queryForObject("select oprresponse from info_topup_transactions", String.class);
        assertThat(operatorResponse, is(notNullValue()));
        assertThat(operatorResponse, is(chargeResponseDesc));
        String serial = jdbcTemplate.queryForObject("select oprtid from info_topup_transactions", String.class);
        assertThat(serial, is(notNullValue()));
        assertThat(serial, is(transactionId));
    }
}
