package com.warehouse.service;

import com.warehouse.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregates data from other services to produce business reports.
 * All reports support a date-range filter.
 */
public class ReportService {

    private static final Logger LOG = LogManager.getLogger(ReportService.class);

    private final InvoiceService invoiceService;
    private final GoodService goodService;
    private final CashService cashService;
    private final ActivityLogService logService;

    public ReportService() {
        this.invoiceService = new InvoiceService();
        this.goodService = new GoodService();
        this.cashService = new CashService();
        this.logService = new ActivityLogService();
    }

    public ReportService(InvoiceService invoiceService, GoodService goodService,
            CashService cashService, ActivityLogService logService) {
        this.invoiceService = invoiceService;
        this.goodService = goodService;
        this.cashService = cashService;
        this.logService = logService;
    }

    // ---- Supplies & Suppliers ------------------------------------------

    /** Returns all purchase invoices in the given period. */
    public List<Invoice> suppliesReport(LocalDate from, LocalDate to) {
        return invoiceService.findPurchasesByDateRange(from, to);
    }

    /** Totals spent on purchases. */
    public BigDecimal totalExpenses(LocalDate from, LocalDate to) {
        return suppliesReport(from, to).stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- Sales & Clients -----------------------------------------------

    /** Returns all sale invoices in the given period. */
    public List<Invoice> salesReport(LocalDate from, LocalDate to) {
        return invoiceService.findSalesByDateRange(from, to);
    }

    /** Total revenue from sales. */
    public BigDecimal totalIncome(LocalDate from, LocalDate to) {
        return salesReport(from, to).stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- Profit --------------------------------------------------------

    /**
     * Profit = income − expenses for the given period.
     *
     * @return positive = profit, negative = loss
     */
    public BigDecimal profitReport(LocalDate from, LocalDate to) {
        BigDecimal income = totalIncome(from, to);
        BigDecimal expenses = totalExpenses(from, to);
        BigDecimal profit = income.subtract(expenses);
        LOG.info("Profit report [{} – {}]: income={} expenses={} profit={}",
                from, to, income, expenses, profit);
        return profit;
    }

    // ---- Stock ---------------------------------------------------------

    /** Returns all active goods (current stock snapshot). */
    public List<Good> stockReport() {
        return goodService.findAllActive();
    }

    /** Returns goods below their minimum threshold. */
    public List<Good> lowStockReport() {
        return goodService.findBelowThreshold();
    }

    // ---- Cash ----------------------------------------------------------

    /** Cash movement for the default register in a period. */
    public List<CashTransaction> cashMovementReport(LocalDate from, LocalDate to) {
        CashRegister reg = cashService.getDefault();
        if (reg == null)
            return List.of();
        return cashService.getTransactionsByDateRange(reg.getId(), from, to);
    }

    // ---- Operator activity ---------------------------------------------

    /** Returns all activity log entries in a date range (for operator reports). */
    public List<ActivityLog> operatorActivityReport(LocalDate from, LocalDate to) {
        return logService.findByDateRange(from, to);
    }
}
