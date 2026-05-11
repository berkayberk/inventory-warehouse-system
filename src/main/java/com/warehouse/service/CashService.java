package com.warehouse.service;

import com.warehouse.dao.CashRegisterDAO;
import com.warehouse.dao.CashTransactionDAO;
import com.warehouse.dao.impl.CashRegisterDAOImpl;
import com.warehouse.dao.impl.CashTransactionDAOImpl;
import com.warehouse.model.*;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Manages cash register balances and transaction history.
 * Each invoice creates a corresponding cash movement through this service.
 */
public class CashService {

    private static final Logger LOG = LogManager.getLogger(CashService.class);

    private final CashRegisterDAO registerDAO;
    private final CashTransactionDAO transactionDAO;
    private final ActivityLogService logService;

    public CashService() {
        this.registerDAO = new CashRegisterDAOImpl();
        this.transactionDAO = new CashTransactionDAOImpl();
        this.logService = new ActivityLogService();
    }

    public CashService(CashRegisterDAO registerDAO, CashTransactionDAO transactionDAO,
            ActivityLogService logService) {
        this.registerDAO = registerDAO;
        this.transactionDAO = transactionDAO;
        this.logService = logService;
    }

    // ---- Register management (Admin only) ------------------------------

    public CashRegister createRegister(String name, BigDecimal initialBalance,
            BigDecimal minThreshold) {
        requireAdmin();
        CashRegister reg = new CashRegister(name, initialBalance, minThreshold);
        registerDAO.save(reg);
        logActor("CREATE_REGISTER", "Created cash register: " + name);
        return reg;
    }

    public void updateRegister(CashRegister reg) {
        requireAdmin();
        registerDAO.update(reg);
        logActor("UPDATE_REGISTER", "Updated register id=" + reg.getId());
    }

    /** Manual deposit (e.g., at start of day). Admin only. */
    public void deposit(int registerId, BigDecimal amount, String description) {
        requireAdmin();
        adjustBalance(registerId, amount, CashTransactionType.DEPOSIT, description, null,
                SessionManager.getCurrentUser().getId());
    }

    /** Manual withdrawal. Admin only. */
    public void withdraw(int registerId, BigDecimal amount, String description) {
        requireAdmin();
        CashRegister reg = getRegisterOrThrow(registerId);
        if (reg.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient cash balance for withdrawal.");
        adjustBalance(registerId, amount.negate(), CashTransactionType.WITHDRAWAL, description, null,
                SessionManager.getCurrentUser().getId());
    }

    // ---- Called internally by InvoiceService ---------------------------

    /** Records income from a sale and credits the register. */
    public void recordIncome(BigDecimal amount, String description, int invoiceId, int operatorId) {
        CashRegister reg = registerDAO.findDefault();
        if (reg == null)
            return; // no register configured yet
        adjustBalance(reg.getId(), amount, CashTransactionType.INCOME,
                description, invoiceId, operatorId);
    }

    /** Records an expense from a purchase and debits the register. */
    public void recordExpense(BigDecimal amount, String description, int invoiceId, int operatorId) {
        CashRegister reg = registerDAO.findDefault();
        if (reg == null)
            return;
        adjustBalance(reg.getId(), amount.negate(), CashTransactionType.EXPENSE,
                description, invoiceId, operatorId);
    }

    // ---- Read ----------------------------------------------------------

    public CashRegister getDefault() {
        return registerDAO.findDefault();
    }

    public List<CashRegister> getAllRegisters() {
        return registerDAO.findAll();
    }

    public List<CashTransaction> getTransactions(int registerId) {
        return transactionDAO.findByRegisterId(registerId);
    }

    public List<CashTransaction> getTransactionsByDateRange(int registerId,
            LocalDate from, LocalDate to) {
        return transactionDAO.findByDateRange(registerId, from, to);
    }

    // ---- Private helpers -----------------------------------------------

    private void adjustBalance(int registerId, BigDecimal delta,
            CashTransactionType type, String description,
            Integer invoiceId, int operatorId) {
        // Load the register fresh to avoid stale balance
        CashRegister reg = getRegisterOrThrow(registerId);
        BigDecimal newBalance = reg.getBalance().add(delta);
        reg.setBalance(newBalance);
        registerDAO.update(reg);

        // Record the transaction
        CashTransaction tx = new CashTransaction(registerId, type,
                delta.abs(), description, invoiceId, operatorId);
        transactionDAO.save(tx);

        LOG.info("Cash {} {} on register id={} → new balance={}",
                type, delta.abs(), registerId, newBalance);

        // Alert if balance dropped below threshold
        if (reg.isBelowThreshold()) {
            LOG.warn("Cash register '{}' is below threshold: balance={}, threshold={}",
                    reg.getName(), newBalance, reg.getMinThreshold());
        }
    }

    private CashRegister getRegisterOrThrow(int id) {
        return registerDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cash register not found: " + id));
    }

    private void requireAdmin() {
        if (!SessionManager.isAdmin())
            throw new SecurityException("Only administrators may manage the cash register.");
    }

    private void logActor(String action, String details) {
        var u = SessionManager.getCurrentUser();
        if (u != null)
            logService.log(u.getId(), u.getUsername(), action, details);
    }
}
