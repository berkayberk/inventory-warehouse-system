package com.warehouse.dao.impl;

import com.warehouse.dao.GoodDAO;
import com.warehouse.model.Good;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link GoodDAO}. */
public class GoodDAOImpl implements GoodDAO {

    private static final Logger LOG = LogManager.getLogger(GoodDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Good save(Good g) {
        String sql = "INSERT INTO goods (name, category, unit, delivery_price, sales_price, "
                + "quantity, min_threshold, active) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getCategory());
            ps.setString(3, g.getUnit());
            ps.setBigDecimal(4, g.getDeliveryPrice());
            ps.setBigDecimal(5, g.getSalesPrice());
            ps.setInt(6, g.getQuantity());
            ps.setInt(7, g.getMinThreshold());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    g.setId(rs.getInt(1));
            }
            LOG.info("Good saved: {}", g.getName());
        } catch (SQLException ex) {
            LOG.error("Error saving good: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save good", ex);
        }
        return g;
    }

    @Override
    public void update(Good g) {
        String sql = "UPDATE goods SET name=?, category=?, unit=?, delivery_price=?, "
                + "sales_price=?, quantity=?, min_threshold=?, active=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getCategory());
            ps.setString(3, g.getUnit());
            ps.setBigDecimal(4, g.getDeliveryPrice());
            ps.setBigDecimal(5, g.getSalesPrice());
            ps.setInt(6, g.getQuantity());
            ps.setInt(7, g.getMinThreshold());
            ps.setBoolean(8, g.isActive());
            ps.setInt(9, g.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error updating good id={}: {}", g.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to update good", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE goods SET active=0 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting good id={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete good", ex);
        }
    }

    @Override
    public Optional<Good> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM goods WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById good: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Good> findAll() {
        List<Good> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM goods ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll goods: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Good> findAllActive() {
        List<Good> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM goods WHERE active=1 ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAllActive goods: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Good> findBelowThreshold() {
        List<Good> list = new ArrayList<>();
        String sql = "SELECT * FROM goods WHERE active=1 AND quantity <= min_threshold ORDER BY quantity";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findBelowThreshold: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Good> searchByNameOrCategory(String keyword) {
        List<Good> list = new ArrayList<>();
        String sql = "SELECT * FROM goods WHERE active=1 "
                + "AND (name LIKE ? OR category LIKE ?) ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("searchByNameOrCategory: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public void adjustQuantity(int goodId, int delta) {
        String sql = "UPDATE goods SET quantity = quantity + ? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, goodId);
            ps.executeUpdate();
            LOG.info("Adjusted quantity for good id={} by {}", goodId, delta);
        } catch (SQLException ex) {
            LOG.error("Error adjusting quantity good id={}: {}", goodId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to adjust quantity", ex);
        }
    }

    private Good map(ResultSet rs) throws SQLException {
        return new Good(
                rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                rs.getString("unit"), rs.getBigDecimal("delivery_price"),
                rs.getBigDecimal("sales_price"), rs.getInt("quantity"),
                rs.getInt("min_threshold"), rs.getBoolean("active"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }
}
