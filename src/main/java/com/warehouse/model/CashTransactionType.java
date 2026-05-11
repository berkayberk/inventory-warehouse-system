package com.warehouse.model;

/**
 * Classifies a cash movement.
 * INCOME – revenue from a sale invoice
 * EXPENSE – cost from a purchase invoice
 * DEPOSIT – manual top-up of the register
 * WITHDRAWAL– manual withdrawal from the register
 */
public enum CashTransactionType {
    INCOME,
    EXPENSE,
    DEPOSIT,
    WITHDRAWAL
}
