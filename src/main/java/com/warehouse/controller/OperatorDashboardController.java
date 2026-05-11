package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.CashRegister;
import com.warehouse.model.Good;
import com.warehouse.service.CashService;
import com.warehouse.service.GoodService;
import com.warehouse.service.UserService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Operator dashboard controller.
 * Operators can monitor stock, create invoices and view reports but
 * cannot manage users, suppliers, clients or cash registers.
 */
public class OperatorDashboardController {

    private static final Logger LOG = LogManager.getLogger(OperatorDashboardController.class);

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label cashBalanceLabel;
    @FXML
    private Label lowStockCountLabel;

    @FXML
    private TableView<Good> stockTable;
    @FXML
    private TableColumn<Good, String> colName;
    @FXML
    private TableColumn<Good, String> colCategory;
    @FXML
    private TableColumn<Good, Integer> colQty;
    @FXML
    private TableColumn<Good, Integer> colMin;

    private final GoodService goodService = new GoodService();
    private final CashService cashService = new CashService();

    @FXML
    public void initialize() {
        var user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + (user != null ? user.getFullName() : "Operator"));

        CashRegister reg = cashService.getDefault();
        cashBalanceLabel.setText(reg != null ? "Cash: " + reg.getBalance() + " BGN" : "Cash: N/A");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));

        List<Good> allGoods = goodService.findAllActive();
        stockTable.setItems(FXCollections.observableArrayList(allGoods));

        List<Good> lowStock = goodService.findBelowThreshold();
        lowStockCountLabel.setText("Low-stock items: " + lowStock.size());
        if (!lowStock.isEmpty()) {
            lowStock.forEach(g -> AlertUtil.showStockAlert(g.getName(), g.getQuantity(), g.getMinThreshold()));
        }
    }

    @FXML
    private void openPurchaseInvoice() {
        MainApp.loadScene("/fxml/purchase_invoice.fxml", "New Purchase Invoice", 1000, 680);
    }

    @FXML
    private void openSaleInvoice() {
        MainApp.loadScene("/fxml/sale_invoice.fxml", "New Sale Invoice", 1000, 680);
    }

    @FXML
    private void openInvoiceList() {
        MainApp.loadScene("/fxml/invoice_list.fxml", "Invoice List", 1100, 650);
    }

    @FXML
    private void openGoodList() {
        MainApp.loadScene("/fxml/good_management.fxml", "Goods / Nomenclature", 1000, 650);
    }

    @FXML
    private void openCashRegister() {
        MainApp.loadScene("/fxml/cash_register.fxml", "Cash Register", 900, 600);
    }

    @FXML
    private void openReports() {
        MainApp.loadScene("/fxml/reports.fxml", "Reports", 1100, 700);
    }

    @FXML
    private void logout() {
        new UserService().logout();
        MainApp.loadScene("/fxml/login.fxml", "Inventory Warehouse – Login", 480, 340);
        MainApp.primaryStage.setResizable(false);
    }
}
