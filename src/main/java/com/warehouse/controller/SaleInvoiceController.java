package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Client;
import com.warehouse.model.Good;
import com.warehouse.service.ClientService;
import com.warehouse.service.GoodService;
import com.warehouse.service.InvoiceService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for creating sale invoices (goods dispatched to client).
 */
public class SaleInvoiceController {

    @FXML
    private ComboBox<Client> clientCombo;
    @FXML
    private TextField notesArea;

    @FXML
    private TableView<Good> goodsTable;
    @FXML
    private TableColumn<Good, String> colGoodName;
    @FXML
    private TableColumn<Good, String> colSalesPrice;
    @FXML
    private TableColumn<Good, Integer> colCurrentQty;

    @FXML
    private TableView<PurchaseInvoiceController.CartItem> cartTable;
    @FXML
    private TableColumn<PurchaseInvoiceController.CartItem, String> colCartName;
    @FXML
    private TableColumn<PurchaseInvoiceController.CartItem, Integer> colCartQty;
    @FXML
    private TableColumn<PurchaseInvoiceController.CartItem, String> colCartSubtotal;

    @FXML
    private TextField qtyField;
    @FXML
    private Label totalLabel;

    private final ClientService clientService = new ClientService();
    private final GoodService goodService = new GoodService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final Map<Integer, PurchaseInvoiceController.CartItem> cart = new HashMap<>();

    @FXML
    public void initialize() {
        clientCombo.setItems(FXCollections.observableArrayList(clientService.findAllActive()));

        colGoodName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSalesPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSalesPrice().toPlainString()));
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

        if (good.getQuantity() < qty) {
            AlertUtil.showWarning("Stock", "Only " + good.getQuantity() + " units available.");
            return;
        }

        cart.merge(good.getId(),
                new PurchaseInvoiceController.CartItem(good.getId(), good.getName(), good.getSalesPrice(), qty),
                (existing, newItem) -> {
                    existing.setQuantity(existing.getQuantity() + qty);
                    return existing;
                });

        refreshCart();
    }

    @FXML
    private void handleRemoveFromCart() {
        var item = cartTable.getSelectionModel().getSelectedItem();
        if (item != null) {
            cart.remove(item.getGoodId());
            refreshCart();
        }
    }

    @FXML
    private void handleSubmit() {
        Client client = clientCombo.getValue();
        if (client == null) {
            AlertUtil.showWarning("Client", "Please select a client.");
            return;
        }
        if (cart.isEmpty()) {
            AlertUtil.showWarning("Cart", "Please add at least one item.");
            return;
        }

        try {
            Map<Integer, Integer> items = new HashMap<>();
            cart.forEach((k, v) -> items.put(k, v.getQuantity()));
            var invoice = invoiceService.createSale(client.getId(), items, notesArea.getText());
            AlertUtil.showInfo("Success", "Sale invoice " + invoice.getInvoiceNumber()
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
                .map(PurchaseInvoiceController.CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).toPlainString());
    }
}
