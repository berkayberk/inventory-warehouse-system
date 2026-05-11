package com.warehouse.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Helper methods for showing JavaFX dialog alerts.
 */
public final class AlertUtil {

    private AlertUtil() {
    }

    /** Displays an informational message. */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Displays a warning message. */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Displays an error message. */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a Yes/No confirmation dialog.
     *
     * @return {@code true} if the user clicked OK/Yes
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Shows a critical-stock notification pop-up. */
    public static void showStockAlert(String goodName, int currentQty, int threshold) {
        String msg = "⚠ LOW STOCK ALERT\n\n"
                + "Product: " + goodName + "\n"
                + "Current Qty: " + currentQty + "\n"
                + "Min Threshold: " + threshold + "\n\n"
                + "Please reorder immediately!";
        showWarning("Stock Alert – " + goodName, msg);
    }

    /** Shows a critical-cash notification pop-up. */
    public static void showCashAlert(String registerName, java.math.BigDecimal balance,
            java.math.BigDecimal threshold) {
        String msg = "⚠ LOW CASH ALERT\n\n"
                + "Register: " + registerName + "\n"
                + "Balance: " + balance + "\n"
                + "Threshold: " + threshold + "\n\n"
                + "Please deposit funds!";
        showWarning("Cash Alert – " + registerName, msg);
    }
}
