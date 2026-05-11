package com.warehouse.dao.impl;

import com.warehouse.dao.InvoiceDAO;
import com.warehouse.model.Invoice;
import com.warehouse.model.InvoiceItem;
import com.warehouse.model.InvoiceType;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link InvoiceDAO}. */
public class InvoiceDAOImpl implements InvoiceDAO {

    private static final Logger LOG = LogManager.getLogger(InvoiceDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ---- Invoice CRUD --------------------------------------------------

    @Override
    public Invoice save(Invoice inv) {
        String sql = "INSERT INTO invoices "
                + "(invoice_number, type, invoice_date, total_amount, supplier_id, "
                + " client_id, operator_id, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getInvoiceNumber());
            ps.setString(2, inv.getType().name());
            ps.setDate(3, Date.valueOf(inv.getInvoiceDate()));
            ps.setBigDecimal(4, inv.getTotalAmount());
            if (inv.getSupplierId() != null)
                ps.setInt(5, inv.getSupplierId());
            else
                ps.setNull(5, Types.INTEGER);
            if (inv.getClientId() != null)
                ps.setInt(6, inv.getClientId());
            else
                ps.setNull(6, Types.INTEGER);
            ps.setInt(7, inv.getOperatorId());
            ps.setString(8, inv.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    inv.setId(rs.getInt(1));
            }
            LOG.info("Invoice saved: {}", inv.getInvoiceNumber());
        } catch (SQLException ex) {
            LOG.error("Error saving invoice: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save invoice", ex);
        }
        return inv;
    }

    @Override
    public void update(Invoice inv) {
        String sql = "UPDATE invoices SET total_amount=?, notes=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, inv.getTotalAmount());
            ps.setString(2, inv.getNotes());
            ps.setInt(3, inv.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error updating invoice: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to update invoice", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM invoices WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting invoice id={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete invoice", ex);
        }
    }

    @Override
    public Optional<Invoice> findById(Integer id) {
        String sql = buildSelectWithJoins() + " WHERE i.id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById invoice: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery(buildSelectWithJoins() + " ORDER BY i.invoice_date DESC")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll invoices: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Invoice> findByType(InvoiceType type) {
        List<Invoice> list = new ArrayList<>();
        String sql = buildSelectWithJoins() + " WHERE i.type=? ORDER BY i.invoice_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByType invoice: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Invoice> findByTypeAndDateRange(InvoiceType type, LocalDate from, LocalDate to) {
        List<Invoice> list = new ArrayList<>();
        String sql = buildSelectWithJoins()
                + " WHERE i.type=? AND i.invoice_date BETWEEN ? AND ?"
                + " ORDER BY i.invoice_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByTypeAndDateRange: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Invoice> findByOperator(int operatorId) {
        List<Invoice> list = new ArrayList<>();
        String sql = buildSelectWithJoins() + " WHERE i.operator_id=? ORDER BY i.invoice_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, operatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByOperator: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Invoice> findBySupplier(int supplierId) {
        List<Invoice> list = new ArrayList<>();
        String sql = buildSelectWithJoins() + " WHERE i.supplier_id=? ORDER BY i.invoice_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findBySupplier: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Invoice> findByClient(int clientId) {
        List<Invoice> list = new ArrayList<>();
        String sql = buildSelectWithJoins() + " WHERE i.client_id=? ORDER BY i.invoice_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findByClient: {}", ex.getMessage(), ex);
        }
        return list;
    }

    // ---- Invoice Items -------------------------------------------------

    @Override
    public List<InvoiceItem> findItemsByInvoiceId(int invoiceId) {
        List<InvoiceItem> list = new ArrayList<>();
        String sql = "SELECT ii.*, g.name AS good_name, g.unit AS good_unit "
                + "FROM invoice_items ii JOIN goods g ON ii.good_id=g.id "
                + "WHERE ii.invoice_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem(
                            rs.getInt("invoice_id"), rs.getInt("good_id"),
                            rs.getInt("quantity"), rs.getBigDecimal("unit_price"));
                    item.setId(rs.getInt("id"));
                    item.setGoodName(rs.getString("good_name"));
                    item.setGoodUnit(rs.getString("good_unit"));
                    list.add(item);
                }
            }
        } catch (SQLException ex) {
            LOG.error("findItemsByInvoiceId: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public InvoiceItem saveItem(InvoiceItem item) {
        String sql = "INSERT INTO invoice_items (invoice_id, good_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getInvoiceId());
            ps.setInt(2, item.getGoodId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    item.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            LOG.error("Error saving invoice item: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save invoice item", ex);
        }
        return item;
    }

    // ---- Helpers -------------------------------------------------------

    private String buildSelectWithJoins() {
        return "SELECT i.*, "
                + "s.name AS supplier_name, c.name AS client_name, u.username AS operator_name "
                + "FROM invoices i "
                + "LEFT JOIN suppliers s ON i.supplier_id = s.id "
                + "LEFT JOIN clients   c ON i.client_id   = c.id "
                + "JOIN  users    u ON i.operator_id = u.id";
    }

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice(
                rs.getString("invoice_number"),
                InvoiceType.valueOf(rs.getString("type")),
                rs.getDate("invoice_date").toLocalDate(),
                rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null,
                rs.getObject("client_id") != null ? rs.getInt("client_id") : null,
                rs.getInt("operator_id"),
                rs.getString("notes"));
        inv.setId(rs.getInt("id"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime()
                : null);
        inv.setSupplierName(rs.getString("supplier_name"));
        inv.setClientName(rs.getString("client_name"));
        inv.setOperatorName(rs.getString("operator_name"));
        return inv;
    }
}
