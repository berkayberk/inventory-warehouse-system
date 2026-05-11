package com.warehouse.dao.impl;

import com.warehouse.dao.CashTransactionDAO;
import com.warehouse.model.CashTransaction;
import com.warehouse.model.CashTransactionType;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link CashTransactionDAO}. */
public class CashTransactionDAOImpl implements CashTransactionDAO {

    private static final Logger LOG = LogManager.getLogger(CashTransactionDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public CashTransaction save(CashTransaction t) {
        String sql = "INSERT INTO cash_transactions "
                + "(register_id, type, amount, description, invoice_id, operator_id, transaction_date) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getRegisterId());
            ps.setString(2, t.getType().name());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getDescription());
            if (t.getInvoiceId() != null)
                ps.setInt(5, t.getInvoiceId());
            else
                ps.setNull(5, Types.INTEGER);
            ps.setInt(6, t.getOperatorId());
            ps.setTimestamp(7, t.getTransactionDate() != null
                    ? Timestamp.valueOf(t.getTransactionDate())
                    : new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    t.setId(rs.getInt(1));
            }
            LOG.info("Cash transaction saved: {} {}", t.getType(), t.getAmount());
        } catch (SQLException ex) {
            LOG.error("Error saving cash transaction: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save cash transaction", ex);
        }
        return t;
    }

    @Override
    public void update(CashTransaction t) {
        /* transactions are immutable */ }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM cash_transactions WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting cash transaction: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete cash transaction", ex);
        }
    }

    @Override
    public Optional<CashTransaction> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT ct.*, u.username AS operator_name, i.invoice_number "
                        + "FROM cash_transactions ct "
                        + "JOIN users u ON ct.operator_id=u.id "
                        + "LEFT JOIN invoices i ON ct.invoice_id=i.id "
                        + "WHERE ct.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById cash tx: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<CashTransaction> findAll() {
        List<CashTransaction> list = new ArrayList<>();
        String sql = "SELECT ct.*, u.username AS operator_name, i.invoice_number "
                + "FROM cash_transactions ct "
                + "JOIN users u ON ct.operator_id=u.id "
                + "LEFT JOIN invoices i ON ct.invoice_id=i.id "
                + "ORDER BY ct.transaction_date DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll cash tx: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<CashTransaction> findByRegisterId(int registerId) {
        List<CashTransaction> list = new ArrayList<>();
        String sql = "SELECT ct.*, u.username AS operator_name, i.invoice_number "
                + "FROM cash_transactions ct "
                + "JOIN users u ON ct.operator_id=u.id "
                + "LEFT JOIN invoices i ON ct.invoice_id=i.id "
                + "WHERE ct.register_id=? ORDER BY ct.transaction_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, registerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByRegisterId cash tx: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<CashTransaction> findByDateRange(int registerId, LocalDate from, LocalDate to) {
        List<CashTransaction> list = new ArrayList<>();
        String sql = "SELECT ct.*, u.username AS operator_name, i.invoice_number "
                + "FROM cash_transactions ct "
                + "JOIN users u ON ct.operator_id=u.id "
                + "LEFT JOIN invoices i ON ct.invoice_id=i.id "
                + "WHERE ct.register_id=? AND DATE(ct.transaction_date) BETWEEN ? AND ? "
                + "ORDER BY ct.transaction_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, registerId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByDateRange cash tx: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<CashTransaction> findByType(int registerId, CashTransactionType type) {
        List<CashTransaction> list = new ArrayList<>();
        String sql = "SELECT ct.*, u.username AS operator_name, i.invoice_number "
                + "FROM cash_transactions ct "
                + "JOIN users u ON ct.operator_id=u.id "
                + "LEFT JOIN invoices i ON ct.invoice_id=i.id "
                + "WHERE ct.register_id=? AND ct.type=? ORDER BY ct.transaction_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, registerId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByType cash tx: {}", ex.getMessage(), ex);
        }
        return list;
    }

    private CashTransaction map(ResultSet rs) throws SQLException {
        CashTransaction t = new CashTransaction(
                rs.getInt("register_id"),
                CashTransactionType.valueOf(rs.getString("type")),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null,
                rs.getInt("operator_id"));
        t.setId(rs.getInt("id"));
        t.setTransactionDate(rs.getTimestamp("transaction_date") != null
                ? rs.getTimestamp("transaction_date").toLocalDateTime()
                : null);
        t.setOperatorName(rs.getString("operator_name"));
        try {
            t.setInvoiceNumber(rs.getString("invoice_number"));
        } catch (Exception ignored) {
        }
        return t;
    }
}
