package com.warehouse.model;

import java.time.LocalDateTime;

/**
 * Represents a system user (Admin or Operator).
 * Encapsulates authentication credentials and role.
 */
public class User {

    private int id;
    private String username;
    private String password; // BCrypt hash stored in DB
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- Constructors --------------------------------------------------

    public User() {
    }

    /** Constructor for creating a new user (no id yet). */
    public User(String username, String password, String fullName, String email, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    /** Full constructor (used when reading from DB). */
    public User(int id, String username, String password, String fullName,
            String email, Role role, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String n) {
        this.fullName = n;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        this.email = e;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role r) {
        this.role = r;
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

    /** Convenience check – does this user have admin privileges? */
    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + '}';
    }
}
