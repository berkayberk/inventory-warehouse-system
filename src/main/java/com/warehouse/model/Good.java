package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a product / nomenclature item stored in the warehouse.
 * Tracks purchase cost, sales price, current stock quantity,
 * and the minimum threshold for low-stock notifications.
 */
public class Good {

    private int id;
    private String name;
    private String category;
    private String unit;
    private BigDecimal deliveryPrice;
    private BigDecimal salesPrice;
    private int quantity;
    private int minThreshold;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- Constructors --------------------------------------------------

    public Good() {
    }

    public Good(String name, String category, String unit,
            BigDecimal deliveryPrice, BigDecimal salesPrice,
            int quantity, int minThreshold) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.deliveryPrice = deliveryPrice;
        this.salesPrice = salesPrice;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.active = true;
    }

    public Good(int id, String name, String category, String unit,
            BigDecimal deliveryPrice, BigDecimal salesPrice,
            int quantity, int minThreshold, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.deliveryPrice = deliveryPrice;
        this.salesPrice = salesPrice;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Business helpers -----------------------------------------------

    /** Returns true when current quantity is at or below the minimum threshold. */
    public boolean isBelowThreshold() {
        return quantity <= minThreshold;
    }

    /** Returns true when stock is completely exhausted. */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    /** Margin = sales price − delivery price */
    public BigDecimal getMargin() {
        return salesPrice.subtract(deliveryPrice);
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String c) {
        this.category = c;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String u) {
        this.unit = u;
    }

    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(BigDecimal dp) {
        this.deliveryPrice = dp;
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal sp) {
        this.salesPrice = sp;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int t) {
        this.minThreshold = t;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean a) {
        this.active = a;
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
        return name + " [" + category + "]";
    }
}
