package com.warehouse.util;

import com.warehouse.model.User;

/**
 * Thread-local session holder that stores the currently logged-in user
 * for the duration of the application session.
 *
 * <p>
 * Because JavaFX runs on a single UI thread in this desktop application,
 * a simple static field is sufficient.
 * </p>
 */
public final class SessionManager {

    private static User currentUser;

    // Prevent instantiation
    private SessionManager() {
    }

    /** Stores the authenticated user after a successful login. */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Returns the currently authenticated user, or {@code null} if not logged in.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Clears the session (called on logout). */
    public static void clearSession() {
        currentUser = null;
    }

    /** Convenience – true when an admin is logged in. */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** @return {@code true} when a user is authenticated. */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
