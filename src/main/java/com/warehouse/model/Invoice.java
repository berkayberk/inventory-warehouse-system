package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice header – covers both PURCHASE (goods in) and SALE (goods out).
 * Items are held in the {@code items} list (loaded lazily by the DAO).
 */
public class Invoice {

    private int id;
    private String invoiceNumber;
    private InvoiceType type;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private Integer supplierId;
    private Integer clientId;
    private int operatorId;
    private String notes;
    private LocalDateTime createdAt;

    // ---------- navigation / transient fields (not in DB columns) -------
    private List<InvoiceItem> items = new ArrayList<>();
    private String supplierName; // denormalized for display
    private String clientName; // denormalized for display
    private String operatorName; // denormalized for display

    // ---- Constructors --------------------------------------------------

    public Invoice() {
    }

    public Invoice(String invoiceNumber, InvoiceType type, LocalDate invoiceDate,
            Integer supplierId, Integer clientId, int operatorId, String notes) {
        this.invoiceNumber = invoiceNumber;
        this.type = type;
        this.invoiceDate = invoiceDate;
        this.supplierId = supplierId;
        this.clientId = clientId;
        this.operatorId = operatorId;
        this.notes = notes;
        this.totalAmount = BigDecimal.ZERO;
    }

    // ---- Business helpers -----------------------------------------------

    /** Recalculate total from line items. */
    public void recalculateTotal() {
        totalAmount = items.stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String n) {
        this.invoiceNumber = n;
    }

    public InvoiceType getType() {
        return type;
    }

    public void setType(InvoiceType t) {
        this.type = t;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate d) {
        this.invoiceDate = d;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal a) {
        this.totalAmount = a;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer sid) {
        this.supplierId = sid;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer cid) {
        this.clientId = cid;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int oid) {
        this.operatorId = oid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String n) {
        this.notes = n;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime d) {
        this.createdAt = d;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> l) {
        this.items = l;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String n) {
        this.supplierName = n;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String n) {
        this.clientName = n;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String n) {
        this.operatorName = n;
    }

    @Override
    public String toString() {
        return invoiceNumber + " [" + type + " " + invoiceDate + " – " + totalAmount + "]";
    }
}
