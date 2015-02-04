package com.infotech.com.it;

import com.infotech.isg.repository.OperatorStatusRepository;
import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.it.fake.mci.MCIWSFake;
import com.infotech.isg.service.ISGOperatorStatusService;

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
 * integration test for MCI operator status service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MCIStatusIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(MCIStatusIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake mci web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    MCIWSFake mciws;

    @Autowired
    ISGOperatorStatusService isgOperatorStatusService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "info_topup_operator_status", "info_topup_operator_last_status");
        jdbcTemplate.update("insert into info_topup_operator_last_status (id,status,timestamp) values(1,'READY',now())");
        jdbcTemplate.update("insert into info_topup_operator_last_status (id,status,timestamp) values(2,'READY',now())");
        jdbcTemplate.update("insert into info_topup_operator_last_status (id,status,timestamp) values(3,'READY',now())");
    }

    @AfterMethod
    public void tearDown() {
        mciws.stop();
    }

    @Test
    public void shouldSetStatusDownIfOperatorNotAvailable() {
        // arrange
        // MCI fake WS not published

        // act
        isgOperatorStatusService.getMCIStatus();

        // assert
        String status = jdbcTemplate.queryForObject("select status from info_topup_operator_last_status where id=2", String.class);
        assertThat(status, is(notNullValue()));
        assertThat(status, is("DOWN"));
    }
}
