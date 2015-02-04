package com.infotech.isg.repository;

import com.infotech.isg.domain.OperatorStatus;

/**
 * repository for Operator status domain object.
 *
 * @author Sevak Gharibian
 */
public interface OperatorStatusRepository {
    public OperatorStatus findById(int id);
    public void update(OperatorStatus operatorStatus);
}
