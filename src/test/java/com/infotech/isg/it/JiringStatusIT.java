package com.infotech.com.it;

import com.infotech.isg.repository.OperatorStatusRepository;
import com.infotech.isg.it.fake.jiring.JiringFake;
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
 * integration test for Jiring operator status service
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class JiringStatusIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(JiringStatusIT.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // fake jiring web service
    // defined as spring managed bean so that app properties can be used
    @Autowired
    JiringFake jiringFake;

    @Autowired
    ISGOperatorStatusService isgOperatorStatusService;

    @BeforeMethod
    public void initDB() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("update info_topup_operator_last_status set status='READY',timestamp=now() where id=1");
        jdbcTemplate.update("update info_topup_operator_last_status set status='READY',timestamp=now() where id=2");
        jdbcTemplate.update("update info_topup_operator_last_status set status='READY',timestamp=now() where id=3");
    }

    @AfterMethod
    public void tearDown() {
        jiringFake.stop();
    }

    @Test
    public void shouldSetStatusDownIfOperatorNotAvailable() {
        // arrange
        // Jiring fake WS not published

        // act
        isgOperatorStatusService.getJiringStatus();

        // assert
        String status = jdbcTemplate.queryForObject("select status from info_topup_operator_last_status where id=3", String.class);
        assertThat(status, is(notNullValue()));
        assertThat(status, is("DOWN"));
    }
}
