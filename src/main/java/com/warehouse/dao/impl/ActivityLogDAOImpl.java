package com.warehouse.dao.impl;

import com.warehouse.dao.ActivityLogDAO;
import com.warehouse.model.ActivityLog;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link ActivityLogDAO}. */
public class ActivityLogDAOImpl implements ActivityLogDAO {

    private static final Logger LOG = LogManager.getLogger(ActivityLogDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public ActivityLog save(ActivityLog log) {
        String sql = "INSERT INTO activity_log (user_id, username, action, details, log_date) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null)
                ps.setInt(1, log.getUserId());
            else
                ps.setNull(1, Types.INTEGER);
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getDetails());
            ps.setTimestamp(5, log.getLogDate() != null
                    ? Timestamp.valueOf(log.getLogDate())
                    : new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    log.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            LOG.error("Error saving activity log: {}", ex.getMessage(), ex);
        }
        return log;
    }

    @Override
    public void update(ActivityLog log) {
        /* logs are immutable */ }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM activity_log WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting activity log id={}: {}", id, ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<ActivityLog> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM activity_log WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById activity log: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<ActivityLog> findAll() {
        List<ActivityLog> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM activity_log ORDER BY log_date DESC")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll activity log: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<ActivityLog> findByUserId(int userId) {
        List<ActivityLog> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM activity_log WHERE user_id=? ORDER BY log_date DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByUserId activity log: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<ActivityLog> findByDateRange(LocalDate from, LocalDate to) {
        List<ActivityLog> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM activity_log WHERE DATE(log_date) BETWEEN ? AND ? "
                        + "ORDER BY log_date DESC")) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByDateRange activity log: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<ActivityLog> findRecent(int limit) {
        List<ActivityLog> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM activity_log ORDER BY log_date DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findRecent activity log: {}", ex.getMessage(), ex);
        }
        return list;
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setDetails(rs.getString("details"));
        log.setLogDate(rs.getTimestamp("log_date") != null
                ? rs.getTimestamp("log_date").toLocalDateTime()
                : null);
        return log;
    }
}
