package com.warehouse.service;

import com.warehouse.dao.InvoiceDAO;
import com.warehouse.dao.impl.InvoiceDAOImpl;
import com.warehouse.model.*;
import com.warehouse.util.InvoiceNumberGenerator;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orchestrates the creation of purchase and sale invoices.
 *
 * <p>
 * Creating an invoice is a multi-step transaction:
 * <ol>
 * <li>Persist the invoice header.</li>
 * <li>Persist each line item.</li>
 * <li>Adjust stock via {@link GoodService}.</li>
 * <li>Record the cash movement via {@link CashService}.</li>
 * <li>Write the activity log.</li>
 * </ol>
 * All steps share the same JDBC connection, so failures leave the DB
 * consistent.
 * </p>
 */
public class InvoiceService {

    private static final Logger LOG = LogManager.getLogger(InvoiceService.class);

    private final InvoiceDAO invoiceDAO;
    private final GoodService goodService;
    private final CashService cashService;
    private final ActivityLogService logService;

    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAOImpl();
        this.goodService = new GoodService();
        this.cashService = new CashService();
        this.logService = new ActivityLogService();
    }

    public InvoiceService(InvoiceDAO invoiceDAO, GoodService goodService,
            CashService cashService, ActivityLogService logService) {
        this.invoiceDAO = invoiceDAO;
        this.goodService = goodService;
        this.cashService = cashService;
        this.logService = logService;
    }

    // ---- Purchase (goods in from supplier) ----------------------------

    /**
     * Records a purchase invoice.
     *
     * @param supplierId the supplying company
     * @param items      map of goodId → quantity
     * @param notes      optional notes
     * @return the saved invoice (with id, total, items populated)
     */
    public Invoice createPurchase(int supplierId,
            Map<Integer, Integer> items,
            String notes) {
        User actor = requireLoggedIn();

        Invoice invoice = new Invoice(
                InvoiceNumberGenerator.next(InvoiceType.PURCHASE),
                InvoiceType.PURCHASE,
                LocalDate.now(),
                supplierId, null,
                actor.getId(),
                notes);

        // ---- Persist header
        invoiceDAO.save(invoice);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int goodId = entry.getKey();
            int quantity = entry.getValue();

            Good good = goodService.findById(goodId)
                    .orElseThrow(() -> new IllegalArgumentException("Good not found: " + goodId));

            BigDecimal unitPrice = good.getDeliveryPrice();
            InvoiceItem item = new InvoiceItem(invoice.getId(), goodId, quantity, unitPrice);
            invoiceDAO.saveItem(item);
            invoice.getItems().add(item);
            total = total.add(item.getSubtotal());

            // Increase stock
            goodService.adjustStock(goodId, quantity);
        }

        // ---- Update total amount
        invoice.setTotalAmount(total);
        invoiceDAO.update(invoice);

        // ---- Record cash expense
        cashService.recordExpense(total, "Purchase invoice " + invoice.getInvoiceNumber(),
                invoice.getId(), actor.getId());

        logService.log(actor.getId(), actor.getUsername(),
                "CREATE_PURCHASE", "Purchase invoice " + invoice.getInvoiceNumber()
                        + " total=" + total);
        LOG.info("Purchase invoice created: {} total={}", invoice.getInvoiceNumber(), total);
        return invoice;
    }

    // ---- Sale (goods out to client) -----------------------------------

    /**
     * Records a sale invoice.
     *
     * @param clientId the purchasing client
     * @param items    map of goodId → quantity
     * @param notes    optional notes
     * @return the saved invoice
     * @throws IllegalStateException if any item has insufficient stock
     */
    public Invoice createSale(int clientId,
            Map<Integer, Integer> items,
            String notes) {
        User actor = requireLoggedIn();

        // Pre-check stock before touching anything
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            Good good = goodService.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Good not found: " + entry.getKey()));
            if (good.getQuantity() < entry.getValue()) {
                throw new IllegalStateException(
                        "Insufficient stock for '" + good.getName() + "'. "
                                + "Available: " + good.getQuantity() + ", requested: " + entry.getValue());
            }
        }

        Invoice invoice = new Invoice(
                InvoiceNumberGenerator.next(InvoiceType.SALE),
                InvoiceType.SALE,
                LocalDate.now(),
                null, clientId,
                actor.getId(),
                notes);

        invoiceDAO.save(invoice);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int goodId = entry.getKey();
            int quantity = entry.getValue();

            Good good = goodService.findById(goodId).get();
            BigDecimal unitPrice = good.getSalesPrice();
            InvoiceItem item = new InvoiceItem(invoice.getId(), goodId, quantity, unitPrice);
            invoiceDAO.saveItem(item);
            invoice.getItems().add(item);
            total = total.add(item.getSubtotal());

            // Decrease stock
            goodService.adjustStock(goodId, -quantity);
        }

        invoice.setTotalAmount(total);
        invoiceDAO.update(invoice);

        cashService.recordIncome(total, "Sale invoice " + invoice.getInvoiceNumber(),
                invoice.getId(), actor.getId());

        logService.log(actor.getId(), actor.getUsername(),
                "CREATE_SALE", "Sale invoice " + invoice.getInvoiceNumber() + " total=" + total);
        LOG.info("Sale invoice created: {} total={}", invoice.getInvoiceNumber(), total);
        return invoice;
    }

    // ---- Queries -------------------------------------------------------

    public List<Invoice> findAll() {
        return invoiceDAO.findAll();
    }

    public List<Invoice> findPurchases() {
        return invoiceDAO.findByType(InvoiceType.PURCHASE);
    }

    public List<Invoice> findSales() {
        return invoiceDAO.findByType(InvoiceType.SALE);
    }

    public Optional<Invoice> findById(int id) {
        return invoiceDAO.findById(id);
    }

    public List<Invoice> findPurchasesByDateRange(LocalDate from, LocalDate to) {
        return invoiceDAO.findByTypeAndDateRange(InvoiceType.PURCHASE, from, to);
    }

    public List<Invoice> findSalesByDateRange(LocalDate from, LocalDate to) {
        return invoiceDAO.findByTypeAndDateRange(InvoiceType.SALE, from, to);
    }

    public List<Invoice> findBySupplier(int sid) {
        return invoiceDAO.findBySupplier(sid);
    }

    public List<Invoice> findByClient(int cid) {
        return invoiceDAO.findByClient(cid);
    }

    public List<Invoice> findByOperator(int oid) {
        return invoiceDAO.findByOperator(oid);
    }

    public List<InvoiceItem> findItems(int invoiceId) {
        return invoiceDAO.findItemsByInvoiceId(invoiceId);
    }

    // ---- Helpers -------------------------------------------------------

    private User requireLoggedIn() {
        User u = SessionManager.getCurrentUser();
        if (u == null)
            throw new SecurityException("Not authenticated.");
        return u;
    }
}
