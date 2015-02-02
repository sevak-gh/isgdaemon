package com.infotech.com.it;

import com.infotech.isg.repository.BalanceRepository;

import javax.sql.DataSource;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * integration test for jdbc repository
 *
 * @author Sevak Gahribian
 */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class JdbcRepositoryIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcRepositoryIT.class);

    @Autowired
    @Qualifier("JdbcBalanceRepository")
    private BalanceRepository balanceRepository;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeClass
    public void initDB() {
        LOG.debug("init db...");
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

    @Test
    public void shouldUpdateMCI10000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI10000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI10000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI20000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI20000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI20000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI50000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI50000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI50000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI100000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI100000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI100000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI200000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI200000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI200000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI500000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI500000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI500000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMCI1000000() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMCI1000000(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MCI1000000 from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateMTN() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateMTN(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select MTN from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }

    @Test
    public void shouldUpdateJiring() {
        // arrange
        Date now = new Date();
        long amount = 1234567890L;

        // act
        balanceRepository.updateJiring(amount, now);

        // assert
        Long balance = jdbcTemplate.queryForObject("select Jiring from info_topup_balance", Long.class);
        assertThat(balance, is(notNullValue()));
        assertThat(balance.longValue(), is(amount));
    }
}
