package com.infotech.isg.repository;

import java.util.List;

import com.infotech.isg.domain.Transaction;

/**
 * repository for Transaction domain object.
 *
 * @author Sevak Gharibian
 */
public interface TransactionRepository {
    public List<Transaction> findByRefNumBankCodeClientId(String refNum, String bankCode, int clientId);
    public List<Transaction> findBySTFProvider(int stf, int provider);
    public void create(Transaction transaction);
    public void update(Transaction transaction);
}
