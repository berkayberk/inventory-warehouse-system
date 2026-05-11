package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Good;
import com.warehouse.service.GoodService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

/** Controller for goods / nomenclature management screen. */
public class GoodManagementController {

    private static final Logger LOG = LogManager.getLogger(GoodManagementController.class);

    @FXML
    private TableView<Good> goodTable;
    @FXML
    private TableColumn<Good, Integer> colId;
    @FXML
    private TableColumn<Good, String> colName;
    @FXML
    private TableColumn<Good, String> colCategory;
    @FXML
    private TableColumn<Good, String> colUnit;
    @FXML
    private TableColumn<Good, String> colDeliveryPrice;
    @FXML
    private TableColumn<Good, String> colSalesPrice;
    @FXML
    private TableColumn<Good, Integer> colQty;
    @FXML
    private TableColumn<Good, String> colStatus;

    @FXML
    private TextField nameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField unitField;
    @FXML
    private TextField deliveryPriceField;
    @FXML
    private TextField salesPriceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField minThresholdField;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox activeCheck;

    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    private final GoodService service = new GoodService();
    private Good selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colDeliveryPrice
                .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryPrice().toPlainString()));
        colSalesPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSalesPrice().toPlainString()));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isOutOfStock() ? "OUT"
                : c.getValue().isBelowThreshold() ? "LOW" : "OK"));

        // Operators can see but admins can edit
        boolean isAdmin = SessionManager.isAdmin();
        saveButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);

        refreshTable();
        goodTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> populateForm(sel));
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        var list = kw.isEmpty() ? service.findAllActive() : service.search(kw);
        goodTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            String unit = unitField.getText().trim();
            BigDecimal dp = new BigDecimal(deliveryPriceField.getText().trim());
            BigDecimal sp = new BigDecimal(salesPriceField.getText().trim());
            int qty = Integer.parseInt(quantityField.getText().trim());
            int threshold = Integer.parseInt(minThresholdField.getText().trim());

            if (name.isEmpty() || unit.isEmpty())
                throw new IllegalArgumentException("Name and Unit are required.");

            if (selected == null) {
                service.create(name, category, unit, dp, sp, qty, threshold);
                AlertUtil.showInfo("Success", "Good created.");
            } else {
                selected.setName(name);
                selected.setCategory(category);
                selected.setUnit(unit);
                selected.setDeliveryPrice(dp);
                selected.setSalesPrice(sp);
                selected.setQuantity(qty);
                selected.setMinThreshold(threshold);
                service.update(selected);
                AlertUtil.showInfo("Success", "Good updated.");
            }
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            AlertUtil.showWarning("Validation", "Please enter valid numeric values for prices and quantities.");
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
            LOG.error("Error saving good: {}", ex.getMessage(), ex);
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) {
            AlertUtil.showWarning("Select", "Please select a good.");
            return;
        }
        if (AlertUtil.showConfirmation("Delete", "Delete good '" + selected.getName() + "'?")) {
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
        refreshTable();
    }

    @FXML
    private void goBack() {
        boolean isAdmin = SessionManager.isAdmin();
        MainApp.loadScene(
                isAdmin ? "/fxml/admin_dashboard.fxml" : "/fxml/operator_dashboard.fxml",
                isAdmin ? "Admin Dashboard" : "Operator Dashboard", 1100, 700);
    }

    private void refreshTable() {
        goodTable.setItems(FXCollections.observableArrayList(service.findAllActive()));
    }

    private void populateForm(Good g) {
        if (g == null)
            return;
        selected = g;
        nameField.setText(g.getName());
        categoryField.setText(g.getCategory() != null ? g.getCategory() : "");
        unitField.setText(g.getUnit());
        deliveryPriceField.setText(g.getDeliveryPrice().toPlainString());
        salesPriceField.setText(g.getSalesPrice().toPlainString());
        quantityField.setText(String.valueOf(g.getQuantity()));
        minThresholdField.setText(String.valueOf(g.getMinThreshold()));
    }

    private void clearForm() {
        selected = null;
        nameField.clear();
        categoryField.clear();
        unitField.clear();
        deliveryPriceField.clear();
        salesPriceField.clear();
        quantityField.clear();
        minThresholdField.clear();
        goodTable.getSelectionModel().clearSelection();
    }
}
