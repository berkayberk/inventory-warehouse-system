package com.warehouse.dao.impl;

import com.warehouse.dao.UserDAO;
import com.warehouse.model.Role;
import com.warehouse.model.User;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDAO}.
 * All SQL statements use PreparedStatements to prevent SQL injection.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOG = LogManager.getLogger(UserDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ---- BaseDAO -------------------------------------------------------

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, role, active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    user.setId(rs.getInt(1));
            }
            LOG.info("User saved: {}", user.getUsername());
        } catch (SQLException ex) {
            LOG.error("Error saving user: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save user", ex);
        }
        return user;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, email=?, role=?, active=? "
                + "WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
            LOG.info("User updated: id={}", user.getId());
        } catch (SQLException ex) {
            LOG.error("Error updating user id={}: {}", user.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to update user", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        // Soft delete – we deactivate rather than remove
        String sql = "UPDATE users SET active=0 WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            LOG.info("User soft-deleted: id={}", id);
        } catch (SQLException ex) {
            LOG.error("Error deleting user id={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete user", ex);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            LOG.error("Error finding user by id={}: {}", id, ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException ex) {
            LOG.error("Error retrieving all users: {}", ex.getMessage(), ex);
        }
        return list;
    }

    // ---- UserDAO -------------------------------------------------------

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            LOG.error("Error finding user by username={}: {}", username, ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAllActive() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active=1 ORDER BY username";
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException ex) {
            LOG.error("Error retrieving active users: {}", ex.getMessage(), ex);
        }
        return list;
    }

    // ---- Mapper --------------------------------------------------------

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("email"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }
}
