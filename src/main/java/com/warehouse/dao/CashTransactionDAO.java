package com.warehouse.dao;

import com.warehouse.model.CashTransaction;
import com.warehouse.model.CashTransactionType;

import java.time.LocalDate;
import java.util.List;

/** Data access contract for {@link CashTransaction} entities. */
public interface CashTransactionDAO extends BaseDAO<CashTransaction, Integer> {

    List<CashTransaction> findByRegisterId(int registerId);

    List<CashTransaction> findByDateRange(int registerId, LocalDate from, LocalDate to);

    List<CashTransaction> findByType(int registerId, CashTransactionType type);
}
