package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single debit or credit movement in a {@link CashRegister}.
 */
public class CashTransaction {

    private int id;
    private int registerId;
    private CashTransactionType type;
    private BigDecimal amount;
    private String description;
    private Integer invoiceId;
    private int operatorId;
    private LocalDateTime transactionDate;

    // transient / navigation
    private String operatorName;
    private String invoiceNumber;

    // ---- Constructors --------------------------------------------------

    public CashTransaction() {
    }

    public CashTransaction(int registerId, CashTransactionType type, BigDecimal amount,
            String description, Integer invoiceId, int operatorId) {
        this.registerId = registerId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.invoiceId = invoiceId;
        this.operatorId = operatorId;
        this.transactionDate = LocalDateTime.now();
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int rid) {
        this.registerId = rid;
    }

    public CashTransactionType getType() {
        return type;
    }

    public void setType(CashTransactionType t) {
        this.type = t;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal a) {
        this.amount = a;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer iid) {
        this.invoiceId = iid;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int oid) {
        this.operatorId = oid;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime d) {
        this.transactionDate = d;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String n) {
        this.operatorName = n;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String n) {
        this.invoiceNumber = n;
    }

    @Override
    public String toString() {
        return type + " " + amount + " [" + transactionDate + "]";
    }
}
