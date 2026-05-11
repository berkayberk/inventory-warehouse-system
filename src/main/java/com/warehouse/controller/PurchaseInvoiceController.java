package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Good;
import com.warehouse.model.Supplier;
import com.warehouse.service.GoodService;
import com.warehouse.service.InvoiceService;
import com.warehouse.service.SupplierService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for creating purchase invoices (goods received from supplier).
 */
public class PurchaseInvoiceController {

    @FXML
    private ComboBox<Supplier> supplierCombo;
    @FXML
    private TextField notesArea;

    // Available goods table
    @FXML
    private TableView<Good> goodsTable;
    @FXML
    private TableColumn<Good, String> colGoodName;
    @FXML
    private TableColumn<Good, String> colDelivery;
    @FXML
    private TableColumn<Good, Integer> colCurrentQty;

    // Cart / order items
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> colCartName;
    @FXML
    private TableColumn<CartItem, Integer> colCartQty;
    @FXML
    private TableColumn<CartItem, String> colCartSubtotal;

    @FXML
    private TextField qtyField;
    @FXML
    private Label totalLabel;

    private final SupplierService supplierService = new SupplierService();
    private final GoodService goodService = new GoodService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final Map<Integer, CartItem> cart = new HashMap<>();

    @FXML
    public void initialize() {
        supplierCombo.setItems(FXCollections.observableArrayList(supplierService.findAllActive()));

        colGoodName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDelivery.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryPrice().toPlainString()));
        colCurrentQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        goodsTable.setItems(FXCollections.observableArrayList(goodService.findAllActive()));

        colCartName.setCellValueFactory(new PropertyValueFactory<>("goodName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartSubtotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().subtotal().toPlainString()));
    }

    @FXML
    private void handleAddToCart() {
        Good good = goodsTable.getSelectionModel().getSelectedItem();
        if (good == null) {
            AlertUtil.showWarning("Select", "Please select a good.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Invalid", "Enter a positive quantity.");
            return;
        }

        cart.merge(good.getId(), new CartItem(good.getId(), good.getName(),
                good.getDeliveryPrice(), qty),
                (existing, newItem) -> {
                    existing.setQuantity(existing.getQuantity() + qty);
                    return existing;
                });

        refreshCart();
    }

    @FXML
    private void handleRemoveFromCart() {
        CartItem item = cartTable.getSelectionModel().getSelectedItem();
        if (item != null) {
            cart.remove(item.getGoodId());
            refreshCart();
        }
    }

    @FXML
    private void handleSubmit() {
        Supplier supplier = supplierCombo.getValue();
        if (supplier == null) {
            AlertUtil.showWarning("Supplier", "Please select a supplier.");
            return;
        }
        if (cart.isEmpty()) {
            AlertUtil.showWarning("Cart", "Please add at least one item.");
            return;
        }

        try {
            Map<Integer, Integer> items = new HashMap<>();
            cart.forEach((k, v) -> items.put(k, v.getQuantity()));
            var invoice = invoiceService.createPurchase(supplier.getId(), items, notesArea.getText());
            AlertUtil.showInfo("Success", "Purchase invoice " + invoice.getInvoiceNumber()
                    + " created. Total: " + invoice.getTotalAmount());
            goBack();
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    @FXML
    private void goBack() {
        boolean isAdmin = SessionManager.isAdmin();
        MainApp.loadScene(
                isAdmin ? "/fxml/admin_dashboard.fxml" : "/fxml/operator_dashboard.fxml",
                isAdmin ? "Admin Dashboard" : "Operator Dashboard", 1100, 700);
    }

    private void refreshCart() {
        cartTable.setItems(FXCollections.observableArrayList(cart.values()));
        totalLabel.setText("Total: " + cart.values().stream()
                .map(CartItem::subtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add).toPlainString());
    }

    // ---- Inner cart DTO ------------------------------------------------
    public static class CartItem {
        private int goodId;
        private String goodName;
        private java.math.BigDecimal unitPrice;
        private int quantity;

        public CartItem(int goodId, String goodName, java.math.BigDecimal unitPrice, int quantity) {
            this.goodId = goodId;
            this.goodName = goodName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public int getGoodId() {
            return goodId;
        }

        public String getGoodName() {
            return goodName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int q) {
            quantity = q;
        }

        public java.math.BigDecimal subtotal() {
            return unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
        }
    }
}
