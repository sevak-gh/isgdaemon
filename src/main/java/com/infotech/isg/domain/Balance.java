package com.infotech.isg.domain;

import java.util.Date;

/**
 * domain object representing isg balance
 *
 * @author Sevak Gharibian
 */
public class Balance {
    private int id;
    private Long mci10000;
    private Long mci20000;
    private Long mci50000;
    private Long mci100000;
    private Long mci200000;
    private Long mci500000;
    private Long mci1000000;
    private Long mtn;
    private Long jiring;
    private Date timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getMCI10000() {
        return mci10000;
    }

    public void setMCI10000(Long mci10000) {
        this.mci10000 = mci10000;
    }

    public Long getMCI20000() {
        return mci20000;
    }

    public void setMCI20000(Long mci20000) {
        this.mci20000 = mci20000;
    }

    public Long getMCI50000() {
        return mci50000;
    }

    public void setMCI50000(Long mci50000) {
        this.mci50000 = mci50000;
    }

    public Long getMCI100000() {
        return mci100000;
    }

    public void setMCI100000(Long mci100000) {
        this.mci100000 = mci100000;
    }

    public Long getMCI200000() {
        return mci200000;
    }

    public void setMCI200000(Long mci200000) {
        this.mci200000 = mci200000;
    }

    public Long getMCI500000() {
        return mci500000;
    }

    public void setMCI500000(Long mci500000) {
        this.mci500000 = mci500000;
    }

    public Long getMCI1000000() {
        return mci1000000;
    }

    public void setMCI1000000(Long mci1000000) {
        this.mci1000000 = mci1000000;
    }

    public Long getMTN() {
        return mtn;
    }

    public void setMTN(Long mtn) {
        this.mtn = mtn;
    }

    public Long getJiring() {
        return jiring;
    }

    public void setJiring(Long jiring) {
        this.jiring = jiring;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Balance[MCI1:%d, MCI2:%d, MCI5:%d, MCI10:%d, MCI20:%d, MCI50:%d, MCI100:%d, MTN:%d, Jiring:%d, timestamp:%s]",
                             mci10000,
                             mci20000,
                             mci50000,
                             mci100000,
                             mci200000,
                             mci500000,
                             mci1000000,
                             mtn,
                             jiring,
                             timestamp);
    }
}
