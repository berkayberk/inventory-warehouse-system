package com.warehouse.dao.impl;

import com.warehouse.dao.SupplierDAO;
import com.warehouse.model.Supplier;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link SupplierDAO}. */
public class SupplierDAOImpl implements SupplierDAO {

    private static final Logger LOG = LogManager.getLogger(SupplierDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Supplier save(Supplier s) {
        String sql = "INSERT INTO suppliers (name, contact, address, phone, email, active) "
                + "VALUES (?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContact());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    s.setId(rs.getInt(1));
            }
            LOG.info("Supplier saved: {}", s.getName());
        } catch (SQLException ex) {
            LOG.error("Error saving supplier: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save supplier", ex);
        }
        return s;
    }

    @Override
    public void update(Supplier s) {
        String sql = "UPDATE suppliers SET name=?, contact=?, address=?, phone=?, email=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContact());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail());
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error updating supplier id={}: {}", s.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to update supplier", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE suppliers SET active=0 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting supplier id={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete supplier", ex);
        }
    }

    @Override
    public Optional<Supplier> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM suppliers WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById supplier: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM suppliers ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll suppliers: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Supplier> findAllActive() {
        List<Supplier> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM suppliers WHERE active=1 ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAllActive suppliers: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Supplier> searchByName(String keyword) {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE active=1 AND name LIKE ? ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("searchByName supplier: {}", ex.getMessage(), ex);
        }
        return list;
    }

    private Supplier map(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("id"), rs.getString("name"), rs.getString("contact"),
                rs.getString("address"), rs.getString("phone"), rs.getString("email"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }
}
