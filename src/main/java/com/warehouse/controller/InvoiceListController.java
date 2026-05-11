package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.Invoice;
import com.warehouse.model.InvoiceItem;
import com.warehouse.service.InvoiceService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Displays all invoices and allows drilling down into their line items.
 */
public class InvoiceListController {

    private static final Logger LOG = LogManager.getLogger(InvoiceListController.class);

    // ---- Invoice table -------------------------------------------------
    @FXML
    private TableView<Invoice> invoiceTable;
    @FXML
    private TableColumn<Invoice, String> colInvNumber;
    @FXML
    private TableColumn<Invoice, String> colInvType;
    @FXML
    private TableColumn<Invoice, String> colInvDate;
    @FXML
    private TableColumn<Invoice, String> colInvParty;
    @FXML
    private TableColumn<Invoice, String> colInvOperator;
    @FXML
    private TableColumn<Invoice, String> colInvTotal;

    // ---- Item detail table ---------------------------------------------
    @FXML
    private TableView<InvoiceItem> itemTable;
    @FXML
    private TableColumn<InvoiceItem, String> colItemGood;
    @FXML
    private TableColumn<InvoiceItem, Integer> colItemQty;
    @FXML
    private TableColumn<InvoiceItem, String> colItemUnit;
    @FXML
    private TableColumn<InvoiceItem, String> colItemPrice;
    @FXML
    private TableColumn<InvoiceItem, String> colItemSubtotal;

    @FXML
    private ToggleGroup filterGroup;
    @FXML
    private RadioButton rbAll;
    @FXML
    private RadioButton rbPurchase;
    @FXML
    private RadioButton rbSale;

    private final InvoiceService service = new InvoiceService();

    @FXML
    public void initialize() {
        colInvNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colInvType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        colInvDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceDate().toString()));
        colInvParty.setCellValueFactory(c -> {
            Invoice inv = c.getValue();
            String cp = inv.getType().name().equals("PURCHASE")
                    ? inv.getSupplierName()
                    : inv.getClientName();
            return new SimpleStringProperty(cp != null ? cp : "");
        });
        colInvTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotalAmount().toPlainString()));
        colInvOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

        colItemGood.setCellValueFactory(new PropertyValueFactory<>("goodName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemUnit.setCellValueFactory(new PropertyValueFactory<>("goodUnit"));
        colItemPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnitPrice().toPlainString()));
        colItemSubtotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubtotal().toPlainString()));

        loadAll();

        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> loadItems(sel));
    }

    @FXML
    private void handleRefresh() {
        Toggle selected = filterGroup.getSelectedToggle();
        if (selected == rbPurchase) {
            invoiceTable.setItems(FXCollections.observableArrayList(service.findPurchases()));
        } else if (selected == rbSale) {
            invoiceTable.setItems(FXCollections.observableArrayList(service.findSales()));
        } else {
            loadAll();
        }
    }

    @FXML
    private void goBack() {
        boolean isAdmin = SessionManager.isAdmin();
        MainApp.loadScene(
                isAdmin ? "/fxml/admin_dashboard.fxml" : "/fxml/operator_dashboard.fxml",
                isAdmin ? "Admin Dashboard" : "Operator Dashboard", 1100, 700);
    }

    private void loadAll() {
        invoiceTable.setItems(FXCollections.observableArrayList(service.findAll()));
    }

    private void loadItems(Invoice inv) {
        if (inv == null) {
            itemTable.getItems().clear();
            return;
        }
        List<InvoiceItem> items = service.findItems(inv.getId());
        itemTable.setItems(FXCollections.observableArrayList(items));
    }
}
