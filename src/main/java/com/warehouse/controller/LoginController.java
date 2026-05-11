package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.User;
import com.warehouse.service.UserService;
import com.warehouse.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Controls the login screen.
 * Authenticates credentials via {@link UserService} and routes
 * to the Admin or Operator dashboard.
 */
public class LoginController {

    private static final Logger LOG = LogManager.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        // Allow Enter key to trigger login
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        loginButton.setDisable(true);

        Optional<User> result = userService.login(username, password);

        if (result.isPresent()) {
            User user = result.get();
            LOG.info("Login successful for: {}", user.getUsername());
            navigateToDashboard(user);
        } else {
            showError("Invalid username or password.");
            passwordField.clear();
            loginButton.setDisable(false);
        }
    }

    private void navigateToDashboard(User user) {
        if (user.isAdmin()) {
            MainApp.loadScene("/fxml/admin_dashboard.fxml",
                    "Admin Dashboard – " + user.getFullName(), 1100, 700);
        } else {
            MainApp.loadScene("/fxml/operator_dashboard.fxml",
                    "Operator Dashboard – " + user.getFullName(), 1100, 700);
        }
        MainApp.primaryStage.setResizable(true);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
