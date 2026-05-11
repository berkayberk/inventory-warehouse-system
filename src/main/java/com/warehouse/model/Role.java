package com.warehouse.model;

/**
 * Defines the two roles available in the system.
 * ADMIN – full access (create users, suppliers, clients, goods, invoices,
 * reports)
 * OPERATOR – limited access (view stock, process invoices, view reports)
 */
public enum Role {
    ADMIN,
    OPERATOR
}
