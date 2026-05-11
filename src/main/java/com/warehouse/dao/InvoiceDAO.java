package com.warehouse.dao;

import com.warehouse.model.Invoice;
import com.warehouse.model.InvoiceItem;
import com.warehouse.model.InvoiceType;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access contract for {@link Invoice} and {@link InvoiceItem} entities.
 */
public interface InvoiceDAO extends BaseDAO<Invoice, Integer> {

    /** Returns all invoices of a given type. */
    List<Invoice> findByType(InvoiceType type);

    /**
     * Returns invoices of a given type within a date range (inclusive).
     * Used by the reporting module.
     */
    List<Invoice> findByTypeAndDateRange(InvoiceType type, LocalDate from, LocalDate to);

    /** Returns invoices created by a specific operator. */
    List<Invoice> findByOperator(int operatorId);

    /** Returns invoices filtered by supplier id. */
    List<Invoice> findBySupplier(int supplierId);

    /** Returns invoices filtered by client id. */
    List<Invoice> findByClient(int clientId);

    /** Loads the line items for an invoice. */
    List<InvoiceItem> findItemsByInvoiceId(int invoiceId);

    /** Saves a line item and returns it with the generated id populated. */
    InvoiceItem saveItem(InvoiceItem item);
}
