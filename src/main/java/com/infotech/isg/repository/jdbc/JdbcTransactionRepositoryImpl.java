package com.infotech.isg.repository.jdbc;

import com.infotech.isg.domain.Transaction;
import com.infotech.isg.repository.TransactionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/**
 * jdbc implementation for Transaction repository.
 *
 * @author Sevak Gharibian
 */
@Repository("JdbcTransactionRepository")
public class JdbcTransactionRepositoryImpl implements TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTransactionRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Transaction> findByRefNumBankCodeClientId(String refNum, String bankCode, int clientId) {
        String sql = "select id, provider, token, type, state, resnum, refnum, revnum, "
                     + "clientip, amount, channel, consumer, bankcode, client, customerip, "
                     + "trtime, bankverify, verifytime, status, operator, oprcommand, "
                     + "oprresponse, oprtid, operatortime, stf, stfresult, opreverse, "
                     + "bkreverse from info_topup_transactions where refnum = ? and "
                     + "bankcode = ? and client = ?";
        return jdbcTemplate.query(sql, new Object[] {refNum, bankCode, clientId}, new TransactionRowMapper());
    }

    @Override
    public List<Transaction> findBySTFProvider(int stf, int provider) {
        String sql = "select id, provider, token, type, state, resnum, refnum, revnum, "
                     + "clientip, amount, channel, consumer, bankcode, client, customerip, "
                     + "trtime, bankverify, verifytime, status, operator, oprcommand, "
                     + "oprresponse, oprtid, operatortime, stf, stfresult, opreverse, "
                     + "bkreverse from info_topup_transactions where stf = ? and "
                     + "provider = ?";
        return jdbcTemplate.query(sql, new Object[] {stf, provider}, new TransactionRowMapper());
    }

    @Override
    public void update(final Transaction transaction) {
        final String sql = "update info_topup_transactions set provider=?, token=?, type=?, "
                           + "state=?, resnum=?, refnum=?, revnum=?, clientip=?, amount=?, "
                           + "channel=?, consumer=?, bankcode=?, client=?, customerip=?, "
                           + "trtime=?, bankverify=?, verifytime=?, status=?, operator=?, "
                           + "oprcommand=?, oprresponse=?, oprtid=?, operatortime=?, stf=?, "
                           + "stfresult=?, opreverse=?, bkreverse=? where id=?";
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps =  connection.prepareStatement(sql);
                ps.setInt(1, transaction.getProvider());
                ps.setString(2, transaction.getToken());
                ps.setInt(3, transaction.getAction());
                ps.setString(4, transaction.getState());
                ps.setString(5, transaction.getResNum());
                ps.setString(6, transaction.getRefNum());
                if (transaction.getRevNum() != null) {
                    ps.setLong(7, transaction.getRevNum());
                } else {
                    ps.setNull(7, java.sql.Types.BIGINT);
                }
                ps.setString(8, transaction.getRemoteIp());
                ps.setLong(9, transaction.getAmount());
                ps.setString(10, transaction.getChannel());
                ps.setString(11, transaction.getConsumer());
                ps.setString(12, transaction.getBankCode());
                ps.setInt(13, transaction.getClientId());
                ps.setString(14, transaction.getCustomerIp());
                ps.setTimestamp(15, new Timestamp(transaction.getTrDateTime().getTime()));
                if (transaction.getBankVerify() != null) {
                    ps.setInt(16, transaction.getBankVerify());
                } else {
                    ps.setNull(16, java.sql.Types.INTEGER);
                }
                if (transaction.getVerifyDateTime() != null) {
                    ps.setTimestamp(17, new Timestamp(transaction.getVerifyDateTime().getTime()));
                } else {
                    ps.setNull(17, java.sql.Types.TIMESTAMP);
                }
                if (transaction.getStatus() != null) {
                    ps.setInt(18, transaction.getStatus());
                } else {
                    ps.setNull(18, java.sql.Types.INTEGER);
                }
                if (transaction.getOperatorResponseCode() != null) {
                    ps.setInt(19, transaction.getOperatorResponseCode());
                } else {
                    ps.setNull(19, java.sql.Types.INTEGER);
                }
                ps.setString(20, transaction.getOperatorCommand());
                ps.setString(21, transaction.getOperatorResponse());
                ps.setString(22, transaction.getOperatorTId());
                if (transaction.getOperatorDateTime() != null) {
                    ps.setTimestamp(23, new Timestamp(transaction.getOperatorDateTime().getTime()));
                } else {
                    ps.setNull(23, java.sql.Types.TIMESTAMP);
                }
                if (transaction.getStf() != null) {
                    ps.setInt(24, transaction.getStf());
                } else {
                    ps.setNull(24, java.sql.Types.TINYINT);
                }
                if (transaction.getStfResult() != null) {
                    ps.setInt(25, transaction.getStfResult());
                } else {
                    ps.setNull(25, java.sql.Types.TINYINT);
                }
                if (transaction.getOpReverse() != null) {
                    ps.setInt(26, transaction.getOpReverse());
                } else {
                    ps.setNull(26, java.sql.Types.TINYINT);
                }
                if (transaction.getBkReverse() != null) {
                    ps.setInt(27, transaction.getBkReverse());
                } else {
                    ps.setNull(27, java.sql.Types.TINYINT);
                }
                ps.setLong(28, transaction.getId());
                return ps;
            }
        });
    }

    @Override
    public void create(final Transaction transaction) {
        final String sql = "insert into info_topup_transactions(provider, token, type, state, resnum, refnum, revnum, "
                           + "clientip, amount, channel, consumer, bankcode, client, customerip, "
                           + "trtime, bankverify, verifytime, status, operator, oprcommand, "
                           + "oprresponse, oprtid, operatortime, stf, stfresult, opreverse, bkreverse) values( "
                           + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps =  connection.prepareStatement(sql, new String[] {"id"});
                ps.setInt(1, transaction.getProvider());
                ps.setString(2, transaction.getToken());
                ps.setInt(3, transaction.getAction());
                ps.setString(4, transaction.getState());
                ps.setString(5, transaction.getResNum());
                ps.setString(6, transaction.getRefNum());
                if (transaction.getRevNum() != null) {
                    ps.setLong(7, transaction.getRevNum());
                } else {
                    ps.setNull(7, java.sql.Types.BIGINT);
                }
                ps.setString(8, transaction.getRemoteIp());
                ps.setLong(9, transaction.getAmount());
                ps.setString(10, transaction.getChannel());
                ps.setString(11, transaction.getConsumer());
                ps.setString(12, transaction.getBankCode());
                ps.setInt(13, transaction.getClientId());
                ps.setString(14, transaction.getCustomerIp());
                ps.setTimestamp(15, new Timestamp(transaction.getTrDateTime().getTime()));
                if (transaction.getBankVerify() != null) {
                    ps.setInt(16, transaction.getBankVerify());
                } else {
                    ps.setNull(16, java.sql.Types.INTEGER);
                }
                if (transaction.getVerifyDateTime() != null) {
                    ps.setTimestamp(17, new Timestamp(transaction.getVerifyDateTime().getTime()));
                } else {
                    ps.setNull(17, java.sql.Types.TIMESTAMP);
                }
                if (transaction.getStatus() != null) {
                    ps.setInt(18, transaction.getStatus());
                } else {
                    ps.setNull(18, java.sql.Types.INTEGER);
                }
                if (transaction.getOperatorResponseCode() != null) {
                    ps.setInt(19, transaction.getOperatorResponseCode());
                } else {
                    ps.setNull(19, java.sql.Types.INTEGER);
                }
                ps.setString(20, transaction.getOperatorCommand());
                ps.setString(21, transaction.getOperatorResponse());
                ps.setString(22, transaction.getOperatorTId());
                if (transaction.getOperatorDateTime() != null) {
                    ps.setTimestamp(23, new Timestamp(transaction.getOperatorDateTime().getTime()));
                } else {
                    ps.setNull(23, java.sql.Types.TIMESTAMP);
                }
                if (transaction.getStf() != null) {
                    ps.setInt(24, transaction.getStf());
                } else {
                    ps.setNull(24, java.sql.Types.TINYINT);
                }
                if (transaction.getStfResult() != null) {
                    ps.setInt(25, transaction.getStfResult());
                } else {
                    ps.setNull(25, java.sql.Types.TINYINT);
                }
                if (transaction.getOpReverse() != null) {
                    ps.setInt(26, transaction.getOpReverse());
                } else {
                    ps.setNull(26, java.sql.Types.TINYINT);
                }
                if (transaction.getBkReverse() != null) {
                    ps.setInt(27, transaction.getBkReverse());
                } else {
                    ps.setNull(27, java.sql.Types.TINYINT);
                }
                return ps;
            }
        }, keyHolder);
        // get generated id
        long id = keyHolder.getKey().longValue();
        transaction.setId(id);
    }

    private static final class TransactionRowMapper implements RowMapper<Transaction> {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getLong("id"));
            transaction.setProvider(rs.getInt("provider"));
            transaction.setToken(rs.getString("token"));
            transaction.setAction(rs.getInt("type"));
            transaction.setState(rs.getString("state"));
            transaction.setResNum(rs.getString("resnum"));
            transaction.setRefNum(rs.getString("refnum"));
            long revNum = rs.getLong("revnum");
            transaction.setRevNum((rs.wasNull()) ? null : new Long(revNum));
            transaction.setRemoteIp(rs.getString("clientip"));
            transaction.setAmount(rs.getLong("amount"));
            transaction.setChannel(rs.getString("channel"));
            transaction.setConsumer(rs.getString("consumer"));
            transaction.setBankCode(rs.getString("bankcode"));
            transaction.setClientId(rs.getInt("client"));
            transaction.setCustomerIp(rs.getString("customerip"));
            transaction.setTrDateTime(rs.getTimestamp("trtime"));
            int bankVerify = rs.getInt("bankverify");
            transaction.setBankVerify((rs.wasNull()) ? null : new Integer(bankVerify));
            transaction.setVerifyDateTime(rs.getTimestamp("verifytime"));
            int status = rs.getInt("status");
            transaction.setStatus((rs.wasNull()) ? null : new Integer(status));
            int operatorResponseCode = rs.getInt("operator");
            transaction.setOperatorResponseCode((rs.wasNull()) ? null : new Integer(operatorResponseCode));
            transaction.setOperatorCommand(rs.getString("oprcommand"));
            transaction.setOperatorResponse(rs.getString("oprresponse"));
            transaction.setOperatorTId(rs.getString("oprtid"));
            transaction.setOperatorDateTime(rs.getTimestamp("operatortime"));
            int stf = rs.getInt("stf");
            transaction.setStf((rs.wasNull()) ? null : new Integer(stf));
            int stfResult = rs.getInt("stfresult");
            transaction.setStfResult((rs.wasNull()) ? null : new Integer(stfResult));
            int opReverse = rs.getInt("opreverse");
            transaction.setOpReverse((rs.wasNull()) ? null : new Integer(opReverse));
            int bkReverse = rs.getInt("bkreverse");
            transaction.setBkReverse((rs.wasNull()) ? null : new Integer(bkReverse));
            return transaction;
        }
    }
}
