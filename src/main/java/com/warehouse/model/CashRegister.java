package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a physical or logical cash register.
 * Holds current balance and triggers alerts when below {@code minThreshold}.
 */
public class CashRegister {

    private int id;
    private String name;
    private BigDecimal balance;
    private BigDecimal minThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- Constructors --------------------------------------------------

    public CashRegister() {
    }

    public CashRegister(String name, BigDecimal balance, BigDecimal minThreshold) {
        this.name = name;
        this.balance = balance;
        this.minThreshold = minThreshold;
    }

    public CashRegister(int id, String name, BigDecimal balance, BigDecimal minThreshold,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.minThreshold = minThreshold;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Business helpers -----------------------------------------------

    /** True when current balance is at or below the critical threshold. */
    public boolean isBelowThreshold() {
        return balance.compareTo(minThreshold) <= 0;
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal b) {
        this.balance = b;
    }

    public BigDecimal getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(BigDecimal t) {
        this.minThreshold = t;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime d) {
        this.createdAt = d;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime d) {
        this.updatedAt = d;
    }

    @Override
    public String toString() {
        return name + " [balance=" + balance + "]";
    }
}
