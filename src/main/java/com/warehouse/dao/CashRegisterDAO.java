package com.warehouse.dao;

import com.warehouse.model.CashRegister;

/** Data access contract for {@link CashRegister} entities. */
public interface CashRegisterDAO extends BaseDAO<CashRegister, Integer> {
    /** Returns the first/default cash register (most deployments have only one). */
    CashRegister findDefault();
}
