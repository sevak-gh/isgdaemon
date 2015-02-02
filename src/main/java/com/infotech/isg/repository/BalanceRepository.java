package com.infotech.isg.repository;

import java.util.Date;

/**
 * repository for isg balance
 *
 * @author Sevak Gharibian
 */
public interface BalanceRepository {
    public void updateMCI10000(long amount, Date timestamp);
    public void updateMCI20000(long amount, Date timestamp);
    public void updateMCI50000(long amount, Date timestamp);
    public void updateMCI100000(long amount, Date timestamp);
    public void updateMCI200000(long amount, Date timestamp);
    public void updateMCI500000(long amount, Date timestamp);
    public void updateMCI1000000(long amount, Date timestamp);
    public void updateMTN(long amount, Date timestamp);
    public void updateJiring(long amount, Date timestamp);
}
