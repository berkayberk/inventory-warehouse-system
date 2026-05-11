package com.warehouse.dao.impl;

import com.warehouse.dao.CashRegisterDAO;
import com.warehouse.model.CashRegister;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link CashRegisterDAO}. */
public class CashRegisterDAOImpl implements CashRegisterDAO {

    private static final Logger LOG = LogManager.getLogger(CashRegisterDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public CashRegister save(CashRegister r) {
        String sql = "INSERT INTO cash_registers (name, balance, min_threshold) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getName());
            ps.setBigDecimal(2, r.getBalance());
            ps.setBigDecimal(3, r.getMinThreshold());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    r.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            LOG.error("Error saving cash register: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save cash register", ex);
        }
        return r;
    }

    @Override
    public void update(CashRegister r) {
        String sql = "UPDATE cash_registers SET name=?, balance=?, min_threshold=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setBigDecimal(2, r.getBalance());
            ps.setBigDecimal(3, r.getMinThreshold());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error updating cash register: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to update cash register", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM cash_registers WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting cash register: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete cash register", ex);
        }
    }

    @Override
    public Optional<CashRegister> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM cash_registers WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById cash register: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<CashRegister> findAll() {
        List<CashRegister> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM cash_registers ORDER BY id")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll cash registers: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public CashRegister findDefault() {
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM cash_registers ORDER BY id LIMIT 1")) {
            if (rs.next())
                return map(rs);
        } catch (SQLException ex) {
            LOG.error("findDefault cash register: {}", ex.getMessage(), ex);
        }
        return null;
    }

    private CashRegister map(ResultSet rs) throws SQLException {
        return new CashRegister(
                rs.getInt("id"), rs.getString("name"),
                rs.getBigDecimal("balance"), rs.getBigDecimal("min_threshold"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }
}
