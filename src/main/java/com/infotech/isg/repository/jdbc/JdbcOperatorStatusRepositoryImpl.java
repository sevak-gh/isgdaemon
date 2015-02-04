package com.infotech.isg.repository.jdbc;

import com.infotech.isg.domain.OperatorStatus;
import com.infotech.isg.repository.OperatorStatusRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jdbc implementation for Operator Status repository.
 *
 * @author Sevak Gharibian
 */
@Repository("JdbcOperatorStatusRepository")
public class JdbcOperatorStatusRepositoryImpl implements OperatorStatusRepository {

    private final Logger LOG = LoggerFactory.getLogger(JdbcOperatorStatusRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcOperatorStatusRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public OperatorStatus findById(int id) {
        OperatorStatus operatorStatus = null;
        String sql = "select id, status, timestamp from info_topup_operator_last_status where id = ?";
        try {
            operatorStatus = jdbcTemplate.queryForObject(sql, new Object[] {id}, new OperatorStatusRowMapper());
        } catch (EmptyResultDataAccessException e) {
            LOG.debug("jdbctemplate empty result set handled", e);
            return null;
        }

        return operatorStatus;
    }

    @Override
    public void update(OperatorStatus operatorStatus) {
        final String sql = "update info_topup_operator_last_status set status=?, timestamp=? where id = ?";
        jdbcTemplate.update(sql, new Object[] {(operatorStatus.getIsAvailable()) ? "READY" : "DOWN", operatorStatus.getTimestamp(), operatorStatus.getId()});
    }

    private static final class OperatorStatusRowMapper implements RowMapper<OperatorStatus> {
        @Override
        public OperatorStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            OperatorStatus operatorStatus = new OperatorStatus();
            operatorStatus.setId(rs.getInt("id"));
            operatorStatus.setIsAvailable(((rs.getString("status").equals("READY")) ? true : false));
            operatorStatus.setTimestamp(rs.getTimestamp("timestamp"));
            return operatorStatus;
        }
    }
}
