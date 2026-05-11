package com.warehouse.dao;

import com.warehouse.model.ActivityLog;

import java.time.LocalDate;
import java.util.List;

/** Data access contract for {@link ActivityLog} entries. */
public interface ActivityLogDAO extends BaseDAO<ActivityLog, Integer> {

    List<ActivityLog> findByUserId(int userId);

    List<ActivityLog> findByDateRange(LocalDate from, LocalDate to);

    /** Returns the most recent {@code limit} entries (for dashboard). */
    List<ActivityLog> findRecent(int limit);
}
