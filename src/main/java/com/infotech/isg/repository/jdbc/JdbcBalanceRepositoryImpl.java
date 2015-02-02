package com.infotech.isg.repository.jdbc;

import com.infotech.isg.repository.BalanceRepository;

import java.util.Date;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * jdbc implementation for balance repository.
 *
 * @author Sevak Gharibian
 */
@Repository("JdbcBalanceRepository")
public class JdbcBalanceRepositoryImpl implements BalanceRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcBalanceRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void updateMCI10000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI10000=?, MCI10000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI20000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI20000=?, MCI20000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI50000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI50000=?, MCI50000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI100000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI100000=?, MCI100000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI200000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI200000=?, MCI200000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI500000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI500000=?, MCI500000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMCI1000000(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MCI1000000=?, MCI1000000Timestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateMTN(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set MTN=?, MTNTimestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }

    @Override
    public void updateJiring(long amount, Date timestamp) {
        final String sql = "update info_topup_balance set Jiring=?, JiringTimestamp=?";
        jdbcTemplate.update(sql, new Object[] {amount, timestamp});
    }
}
