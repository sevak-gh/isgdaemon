package com.infotech.isg.domain;

import java.util.Date;

/**
 * domain object representing oprtaot status.
 *
 * @author Sevak Gharibian
 */
public class OperatorStatus {

    private int id;
    private boolean isAvailable;
    private Date timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("OperatorStatus[id:%d, isAvailable:%s, timestamp: %s]", id, Boolean.toString(isAvailable), timestamp.toString());
    }
}
