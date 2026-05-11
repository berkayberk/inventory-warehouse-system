package com.warehouse.service;

import com.warehouse.dao.SupplierDAO;
import com.warehouse.dao.impl.SupplierDAOImpl;
import com.warehouse.model.Supplier;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for supplier management. Only admins may create/edit/delete.
 */
public class SupplierService {

    private static final Logger LOG = LogManager.getLogger(SupplierService.class);

    private final SupplierDAO supplierDAO;
    private final ActivityLogService logService;

    public SupplierService() {
        this.supplierDAO = new SupplierDAOImpl();
        this.logService = new ActivityLogService();
    }

    public SupplierService(SupplierDAO supplierDAO, ActivityLogService logService) {
        this.supplierDAO = supplierDAO;
        this.logService = logService;
    }

    public Supplier create(String name, String contact, String address,
            String phone, String email) {
        requireAdmin();
        Supplier s = new Supplier(name, contact, address, phone, email);
        supplierDAO.save(s);
        logActor("CREATE_SUPPLIER", "Created supplier: " + name);
        LOG.info("Supplier created: {}", name);
        return s;
    }

    public void update(Supplier supplier) {
        requireAdmin();
        supplierDAO.update(supplier);
        logActor("UPDATE_SUPPLIER", "Updated supplier id=" + supplier.getId());
        LOG.info("Supplier updated: id={}", supplier.getId());
    }

    public void delete(int id) {
        requireAdmin();
        supplierDAO.delete(id);
        logActor("DELETE_SUPPLIER", "Deleted supplier id=" + id);
        LOG.info("Supplier deleted: id={}", id);
    }

    public List<Supplier> findAll() {
        return supplierDAO.findAll();
    }

    public List<Supplier> findAllActive() {
        return supplierDAO.findAllActive();
    }

    public List<Supplier> search(String kw) {
        return supplierDAO.searchByName(kw);
    }

    public Optional<Supplier> findById(int id) {
        return supplierDAO.findById(id);
    }

    private void requireAdmin() {
        if (!SessionManager.isAdmin())
            throw new SecurityException("Only administrators may manage suppliers.");
    }

    private void logActor(String action, String details) {
        var u = SessionManager.getCurrentUser();
        if (u != null)
            logService.log(u.getId(), u.getUsername(), action, details);
    }
}
