package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Supplier;
import com.warehouse.service.SupplierService;
import com.warehouse.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** CRUD controller for suppliers. */
public class SupplierManagementController {

    private static final Logger LOG = LogManager.getLogger(SupplierManagementController.class);

    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, Integer> colId;
    @FXML
    private TableColumn<Supplier, String> colName;
    @FXML
    private TableColumn<Supplier, String> colContact;
    @FXML
    private TableColumn<Supplier, String> colPhone;
    @FXML
    private TableColumn<Supplier, String> colActive;

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

    private final SupplierService service = new SupplierService();
    private Supplier selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Yes" : "No"));
        refreshTable();
        activeCheck.setSelected(true);
        supplierTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> populateForm(sel));
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        var list = kw.isEmpty() ? service.findAllActive() : service.search(kw);
        supplierTable.setItems(FXCollections.observableArrayList(list));
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
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) {
            AlertUtil.showWarning("Select", "Please select a supplier.");
            return;
        }
        if (AlertUtil.showConfirmation("Delete", "Delete supplier '" + selected.getName() + "'?")) {
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
        supplierTable.setItems(FXCollections.observableArrayList(service.findAllActive()));
    }

    private void populateForm(Supplier s) {
        if (s == null)
            return;
        selected = s;
        nameField.setText(s.getName());
        contactField.setText(s.getContact() != null ? s.getContact() : "");
        addressArea.setText(s.getAddress() != null ? s.getAddress() : "");
        phoneField.setText(s.getPhone() != null ? s.getPhone() : "");
        emailField.setText(s.getEmail() != null ? s.getEmail() : "");
        activeCheck.setSelected(s.isActive());
    }

    private void clearForm() {
        selected = null;
        nameField.clear();
        contactField.clear();
        addressArea.clear();
        phoneField.clear();
        emailField.clear();
        activeCheck.setSelected(true);
        supplierTable.getSelectionModel().clearSelection();
    }
}
