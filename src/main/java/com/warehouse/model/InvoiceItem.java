package com.warehouse.model;

import java.math.BigDecimal;

/**
 * A single line item on an invoice.
 * Holds the good reference, quantity ordered, and unit price at the time
 * of the transaction (snapshot – independent from the current goods price).
 */
public class InvoiceItem {

    private int id;
    private int invoiceId;
    private int goodId;
    private int quantity;
    private BigDecimal unitPrice;

    // transient / navigation
    private String goodName;
    private String goodUnit;

    // ---- Constructors --------------------------------------------------

    public InvoiceItem() {
    }

    public InvoiceItem(int invoiceId, int goodId, int quantity, BigDecimal unitPrice) {
        this.invoiceId = invoiceId;
        this.goodId = goodId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // ---- Business helpers -----------------------------------------------

    /** subtotal = quantity × unitPrice */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int iid) {
        this.invoiceId = iid;
    }

    public int getGoodId() {
        return goodId;
    }

    public void setGoodId(int gid) {
        this.goodId = gid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal up) {
        this.unitPrice = up;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String n) {
        this.goodName = n;
    }

    public String getGoodUnit() {
        return goodUnit;
    }

    public void setGoodUnit(String u) {
        this.goodUnit = u;
    }

    @Override
    public String toString() {
        return goodName + " × " + quantity + " @ " + unitPrice;
    }
}
