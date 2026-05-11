package com.warehouse.service;

import com.warehouse.dao.ActivityLogDAO;
import com.warehouse.dao.impl.ActivityLogDAOImpl;
import com.warehouse.model.ActivityLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for creating and querying the in-DB audit trail.
 * All write-through calls to other services should call
 * {@link #log(Integer, String, String, String)} to persist events.
 */
public class ActivityLogService {

    private static final Logger LOG = LogManager.getLogger(ActivityLogService.class);

    private final ActivityLogDAO logDAO;

    public ActivityLogService() {
        this.logDAO = new ActivityLogDAOImpl();
    }

    /** Constructor for dependency injection. */
    public ActivityLogService(ActivityLogDAO logDAO) {
        this.logDAO = logDAO;
    }

    /**
     * Persists a single activity entry.
     *
     * @param userId   nullable – null for system/anonymous events
     * @param username actor username
     * @param action   short action code (e.g. "CREATE_INVOICE")
     * @param details  free-text detail
     */
    public void log(Integer userId, String username, String action, String details) {
        try {
            ActivityLog entry = new ActivityLog(userId, username, action, details);
            logDAO.save(entry);
        } catch (Exception ex) {
            // Logging should never crash the application
            LOG.error("Failed to persist activity log: {}", ex.getMessage(), ex);
        }
    }

    public List<ActivityLog> findAll() {
        return logDAO.findAll();
    }

    public List<ActivityLog> findByUserId(int userId) {
        return logDAO.findByUserId(userId);
    }

    public List<ActivityLog> findByDateRange(LocalDate from, LocalDate to) {
        return logDAO.findByDateRange(from, to);
    }

    public List<ActivityLog> findRecent(int limit) {
        return logDAO.findRecent(limit);
    }
}
