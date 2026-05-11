package com.warehouse.service;

import com.warehouse.dao.GoodDAO;
import com.warehouse.dao.impl.GoodDAOImpl;
import com.warehouse.model.Good;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for product (nomenclature) management.
 * Admins create/edit/delete; operators may view and search.
 */
public class GoodService {

    private static final Logger LOG = LogManager.getLogger(GoodService.class);

    private final GoodDAO goodDAO;
    private final ActivityLogService logService;

    public GoodService() {
        this.goodDAO = new GoodDAOImpl();
        this.logService = new ActivityLogService();
    }

    public GoodService(GoodDAO goodDAO, ActivityLogService logService) {
        this.goodDAO = goodDAO;
        this.logService = logService;
    }

    /** Creates a new product in the nomenclature. Admin only. */
    public Good create(String name, String category, String unit,
            BigDecimal deliveryPrice, BigDecimal salesPrice,
            int quantity, int minThreshold) {
        requireAdmin();
        validatePrices(deliveryPrice, salesPrice);

        Good g = new Good(name, category, unit, deliveryPrice, salesPrice, quantity, minThreshold);
        goodDAO.save(g);
        logActor("CREATE_GOOD", "Created good: " + name);
        LOG.info("Good created: {}", name);
        return g;
    }

    /** Saves all editable fields on an existing good. Admin only. */
    public void update(Good good) {
        requireAdmin();
        validatePrices(good.getDeliveryPrice(), good.getSalesPrice());
        goodDAO.update(good);
        logActor("UPDATE_GOOD", "Updated good id=" + good.getId());
        LOG.info("Good updated: id={}", good.getId());
    }

    /** Soft-deletes a good. Admin only. */
    public void delete(int id) {
        requireAdmin();
        goodDAO.delete(id);
        logActor("DELETE_GOOD", "Deleted good id=" + id);
    }

    // ---- Read ----------------------------------------------------------

    public List<Good> findAll() {
        return goodDAO.findAll();
    }

    public List<Good> findAllActive() {
        return goodDAO.findAllActive();
    }

    public List<Good> findBelowThreshold() {
        return goodDAO.findBelowThreshold();
    }

    public List<Good> search(String kw) {
        return goodDAO.searchByNameOrCategory(kw);
    }

    public Optional<Good> findById(int id) {
        return goodDAO.findById(id);
    }

    // ---- Internal stock adjustment (called by InvoiceService) ----------

    /**
     * Increases or decreases stock.
     * delta > 0 → purchase / incoming goods
     * delta < 0 → sale / outgoing goods
     *
     * @throws IllegalStateException if a sale would push stock negative
     */
    public void adjustStock(int goodId, int delta) {
        if (delta < 0) {
            Good g = goodDAO.findById(goodId)
                    .orElseThrow(() -> new IllegalArgumentException("Good not found: " + goodId));
            if (g.getQuantity() + delta < 0) {
                throw new IllegalStateException(
                        "Insufficient stock for '" + g.getName() + "'. "
                                + "Available: " + g.getQuantity() + ", requested: " + Math.abs(delta));
            }
        }
        goodDAO.adjustQuantity(goodId, delta);
    }

    // ---- Helpers -------------------------------------------------------

    private void requireAdmin() {
        if (!SessionManager.isAdmin())
            throw new SecurityException("Only administrators may manage goods.");
    }

    private void validatePrices(BigDecimal delivery, BigDecimal sales) {
        if (delivery == null || delivery.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Delivery price must be ≥ 0.");
        if (sales == null || sales.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Sales price must be ≥ 0.");
    }

    private void logActor(String action, String details) {
        var u = SessionManager.getCurrentUser();
        if (u != null)
            logService.log(u.getId(), u.getUsername(), action, details);
    }
}
