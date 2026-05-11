package com.warehouse.model;

import java.time.LocalDateTime;

/**
 * Persists operator / admin actions to the database for audit purposes.
 * This complements the file-based log4j logging.
 */
public class ActivityLog {

    private int id;
    private Integer userId; // nullable – system events have no user
    private String username;
    private String action;
    private String details;
    private LocalDateTime logDate;

    // ---- Constructors --------------------------------------------------

    public ActivityLog() {
    }

    public ActivityLog(Integer userId, String username, String action, String details) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.details = details;
        this.logDate = LocalDateTime.now();
    }

    // ---- Getters & Setters ---------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer uid) {
        this.userId = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String a) {
        this.action = a;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String d) {
        this.details = d;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDateTime d) {
        this.logDate = d;
    }

    @Override
    public String toString() {
        return "[" + logDate + "] " + username + ": " + action;
    }
}
