package com.warehouse.service;

import com.warehouse.dao.UserDAO;
import com.warehouse.dao.impl.UserDAOImpl;
import com.warehouse.model.Role;
import com.warehouse.model.User;
import com.warehouse.util.PasswordUtil;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for user management and authentication.
 * Enforces constraints (unique username, password rules) before delegating to
 * the DAO.
 */
public class UserService {

    private static final Logger LOG = LogManager.getLogger(UserService.class);

    private final UserDAO userDAO;
    private final ActivityLogService logService;

    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.logService = new ActivityLogService();
    }

    /** Constructor for dependency injection (testing). */
    public UserService(UserDAO userDAO, ActivityLogService logService) {
        this.userDAO = userDAO;
        this.logService = logService;
    }

    // ---- Authentication ------------------------------------------------

    /**
     * Validates username and password.
     * On success stores the user in {@link SessionManager} and logs the event.
     *
     * @return the authenticated user or empty if credentials are invalid
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null)
            return Optional.empty();

        Optional<User> opt = userDAO.findByUsername(username);
        if (opt.isPresent()) {
            User user = opt.get();
            if (!user.isActive()) {
                LOG.warn("Login attempt for inactive account: {}", username);
                return Optional.empty();
            }
            if (PasswordUtil.verify(password, user.getPassword())) {
                SessionManager.setCurrentUser(user);
                logService.log(user.getId(), user.getUsername(),
                        "LOGIN", "Successful login");
                LOG.info("User logged in: {}", username);
                return Optional.of(user);
            } else {
                logService.log(null, username, "LOGIN_FAILED", "Bad password");
                LOG.warn("Failed login attempt for username: {}", username);
            }
        } else {
            LOG.warn("Login attempt for unknown username: {}", username);
        }
        return Optional.empty();
    }

    /** Clears the session and logs the logout event. */
    public void logout() {
        User u = SessionManager.getCurrentUser();
        if (u != null) {
            logService.log(u.getId(), u.getUsername(), "LOGOUT", "User logged out");
            LOG.info("User logged out: {}", u.getUsername());
        }
        SessionManager.clearSession();
    }

    // ---- CRUD ----------------------------------------------------------

    /**
     * Creates a new user. Only admins may call this.
     *
     * @param username  login name (must be unique)
     * @param plainPass plain-text password (will be hashed)
     * @param fullName  display name
     * @param email     optional email
     * @param role      ADMIN or OPERATOR
     * @return the saved user
     * @throws IllegalArgumentException if username already exists
     */
    public User createUser(String username, String plainPass, String fullName,
            String email, Role role) {
        requireAdmin();
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        validatePassword(plainPass);

        User user = new User(username, PasswordUtil.hash(plainPass), fullName, email, role);
        userDAO.save(user);

        User actor = SessionManager.getCurrentUser();
        logService.log(actor.getId(), actor.getUsername(),
                "CREATE_USER", "Created user: " + username + " role=" + role);
        LOG.info("Created user: {}", username);
        return user;
    }

    /**
     * Updates user details. Admins only.
     *
     * @param newPlainPass if null or blank, the password is left unchanged
     */
    public void updateUser(User user, String newPlainPass) {
        requireAdmin();
        if (newPlainPass != null && !newPlainPass.isBlank()) {
            validatePassword(newPlainPass);
            user.setPassword(PasswordUtil.hash(newPlainPass));
        }
        userDAO.update(user);

        User actor = SessionManager.getCurrentUser();
        logService.log(actor.getId(), actor.getUsername(),
                "UPDATE_USER", "Updated user id=" + user.getId());
        LOG.info("Updated user id={}", user.getId());
    }

    /** Soft-deletes a user. Admins only. */
    public void deleteUser(int userId) {
        requireAdmin();
        userDAO.delete(userId);
        User actor = SessionManager.getCurrentUser();
        logService.log(actor.getId(), actor.getUsername(),
                "DELETE_USER", "Deleted user id=" + userId);
        LOG.info("Soft-deleted user id={}", userId);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userDAO.findAllActive();
    }

    public Optional<User> findById(int id) {
        return userDAO.findById(id);
    }

    public Optional<User> findByUsername(String name) {
        return userDAO.findByUsername(name);
    }

    // ---- Helpers -------------------------------------------------------

    private void requireAdmin() {
        if (!SessionManager.isAdmin()) {
            throw new SecurityException("Only administrators may perform this action.");
        }
    }

    private void validatePassword(String pass) {
        if (pass == null || pass.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }
}
