package com.warehouse.dao.impl;

import com.warehouse.dao.ClientDAO;
import com.warehouse.model.Client;
import com.warehouse.util.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of {@link ClientDAO}. */
public class ClientDAOImpl implements ClientDAO {

    private static final Logger LOG = LogManager.getLogger(ClientDAOImpl.class);

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Client save(Client c) {
        String sql = "INSERT INTO clients (name, contact, address, phone, email, active) "
                + "VALUES (?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getContact());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    c.setId(rs.getInt(1));
            }
            LOG.info("Client saved: {}", c.getName());
        } catch (SQLException ex) {
            LOG.error("Error saving client: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save client", ex);
        }
        return c;
    }

    @Override
    public void update(Client c) {
        String sql = "UPDATE clients SET name=?, contact=?, address=?, phone=?, email=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getContact());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error updating client id={}: {}", c.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to update client", ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE clients SET active=0 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error deleting client id={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete client", ex);
        }
    }

    @Override
    public Optional<Client> findById(Integer id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM clients WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("findById client: {}", ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM clients ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAll clients: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Client> findAllActive() {
        List<Client> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM clients WHERE active=1 ORDER BY name")) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException ex) {
            LOG.error("findAllActive clients: {}", ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<Client> searchByName(String keyword) {
        List<Client> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM clients WHERE active=1 AND name LIKE ? ORDER BY name")) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            LOG.error("searchByName client: {}", ex.getMessage(), ex);
        }
        return list;
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"), rs.getString("name"), rs.getString("contact"),
                rs.getString("address"), rs.getString("phone"), rs.getString("email"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }
}
