package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Client;
import com.warehouse.service.ClientService;
import com.warehouse.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** CRUD controller for clients. */
public class ClientManagementController {

    private static final Logger LOG = LogManager.getLogger(ClientManagementController.class);

    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> colId;
    @FXML
    private TableColumn<Client, String> colName;
    @FXML
    private TableColumn<Client, String> colContact;
    @FXML
    private TableColumn<Client, String> colPhone;
    @FXML
    private TableColumn<Client, String> colActive;

    @FXML
    private TextField nameField;
    @FXML
    private TextField contactField;
    @FXML
    private TextArea addressArea;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox activeCheck;

    private final ClientService service = new ClientService();
    private Client selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Yes" : "No"));
        refreshTable();
        activeCheck.setSelected(true);
        clientTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> populateForm(sel));
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        var list = kw.isEmpty() ? service.findAllActive() : service.search(kw);
        clientTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showWarning("Validation", "Name is required.");
            return;
        }
        try {
            if (selected == null) {
                service.create(name, contactField.getText(), addressArea.getText(),
                        phoneField.getText(), emailField.getText());
            } else {
                selected.setName(name);
                selected.setContact(contactField.getText());
                selected.setAddress(addressArea.getText());
                selected.setPhone(phoneField.getText());
                selected.setEmail(emailField.getText());
                selected.setActive(activeCheck.isSelected());
                service.update(selected);
            }
            clearForm();
            refreshTable();
            AlertUtil.showInfo("Success", "Client saved.");
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) {
            AlertUtil.showWarning("Select", "Please select a client.");
            return;
        }
        if (AlertUtil.showConfirmation("Delete", "Delete client '" + selected.getName() + "'?")) {
            service.delete(selected.getId());
            clearForm();
            refreshTable();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
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

    private void refreshTable() {
        clientTable.setItems(FXCollections.observableArrayList(service.findAllActive()));
    }

    private void populateForm(Client c) {
        if (c == null)
            return;
        selected = c;
        nameField.setText(c.getName());
        contactField.setText(c.getContact() != null ? c.getContact() : "");
        addressArea.setText(c.getAddress() != null ? c.getAddress() : "");
        phoneField.setText(c.getPhone() != null ? c.getPhone() : "");
        emailField.setText(c.getEmail() != null ? c.getEmail() : "");
        activeCheck.setSelected(c.isActive());
    }

    private void clearForm() {
        selected = null;
        nameField.clear();
        contactField.clear();
        addressArea.clear();
        phoneField.clear();
        emailField.clear();
        activeCheck.setSelected(true);
        clientTable.getSelectionModel().clearSelection();
    }
}
