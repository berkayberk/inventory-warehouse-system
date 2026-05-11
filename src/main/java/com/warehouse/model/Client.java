package com.warehouse.model;

import java.time.LocalDateTime;

/**
 * Represents a client who purchases goods from the warehouse.
 */
public class Client {

    private int id;
    private String name;
    private String contact;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;

    // ---- Constructors --------------------------------------------------

    public Client() {
    }

    public Client(String name, String contact, String address, String phone, String email) {
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.active = true;
    }

    public Client(int id, String name, String contact, String address,
            String phone, String email, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String c) {
        this.contact = c;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String a) {
        this.address = a;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String p) {
        this.phone = p;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        this.email = e;
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

    @Override
    public String toString() {
        return name;
    }
}
