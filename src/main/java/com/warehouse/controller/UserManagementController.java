package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Role;
import com.warehouse.model.User;
import com.warehouse.service.UserService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages administrator CRUD operations for system users.
 */
public class UserManagementController {

    private static final Logger LOG = LogManager.getLogger(UserManagementController.class);

    // ---- Table ---------------------------------------------------------
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colFullName;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colActive;

    // ---- Form fields ---------------------------------------------------
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<Role> roleCombo;
    @FXML
    private CheckBox activeCheck;

    @FXML
    private TextField searchField;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();
    private User selectedUser = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Yes" : "No"));

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.getSelectionModel().select(Role.OPERATOR);

        refreshTable();

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> populateForm(sel));
    }

    // ---- FXML handlers -------------------------------------------------

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        Role role = roleCombo.getValue();

        if (username.isEmpty() || fullName.isEmpty() || role == null) {
            AlertUtil.showWarning("Validation", "Username, Full Name and Role are required.");
            return;
        }

        try {
            if (selectedUser == null) {
                // Create
                if (password.isEmpty()) {
                    AlertUtil.showWarning("Validation", "Password is required for new users.");
                    return;
                }
                userService.createUser(username, password, fullName, email, role);
                AlertUtil.showInfo("Success", "User created successfully.");
            } else {
                // Update
                selectedUser.setUsername(username);
                selectedUser.setFullName(fullName);
                selectedUser.setEmail(email);
                selectedUser.setRole(role);
                selectedUser.setActive(activeCheck.isSelected());
                userService.updateUser(selectedUser, password.isEmpty() ? null : password);
                AlertUtil.showInfo("Success", "User updated successfully.");
            }
            clearForm();
            refreshTable();
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
            LOG.error("Error saving user: {}", ex.getMessage(), ex);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            AlertUtil.showWarning("Select a user", "Please select a user to delete.");
            return;
        }
        if (selectedUser.getId() == SessionManager.getCurrentUser().getId()) {
            AlertUtil.showWarning("Not allowed", "You cannot delete your own account.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm Delete",
                "Deactivate user '" + selectedUser.getUsername() + "'?")) {
            userService.deleteUser(selectedUser.getId());
            clearForm();
            refreshTable();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            refreshTable();
            return;
        }
        userTable.setItems(FXCollections.observableArrayList(
                userService.getAllUsers().stream()
                        .filter(u -> u.getUsername().toLowerCase().contains(kw)
                                || u.getFullName().toLowerCase().contains(kw))
                        .toList()));
    }

    @FXML
    private void handleLoadAll() {
        if (searchField != null)
            searchField.clear();
        refreshTable();
    }

    @FXML
    private void goBack() {
        MainApp.loadScene("/fxml/admin_dashboard.fxml", "Admin Dashboard", 1100, 700);
    }

    // ---- Helpers -------------------------------------------------------

    private void refreshTable() {
        userTable.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
    }

    private void populateForm(User u) {
        if (u == null)
            return;
        selectedUser = u;
        usernameField.setText(u.getUsername());
        passwordField.clear();
        fullNameField.setText(u.getFullName());
        emailField.setText(u.getEmail() != null ? u.getEmail() : "");
        roleCombo.getSelectionModel().select(u.getRole());
        activeCheck.setSelected(u.isActive());
    }

    private void clearForm() {
        selectedUser = null;
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        emailField.clear();
        roleCombo.getSelectionModel().select(Role.OPERATOR);
        activeCheck.setSelected(true);
        userTable.getSelectionModel().clearSelection();
    }
}
