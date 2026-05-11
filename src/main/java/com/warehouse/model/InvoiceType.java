package com.warehouse.model;

/**
 * Distinguishes between incoming (purchase from supplier) and
 * outgoing (sale to client) invoices.
 */
public enum InvoiceType {
    PURCHASE,
    SALE
}
