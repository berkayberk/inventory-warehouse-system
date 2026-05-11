package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.ActivityLog;
import com.warehouse.model.CashRegister;
import com.warehouse.model.Good;
import com.warehouse.service.*;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin dashboard controller.
 *
 * <p>
 * Shows summary widgets (stock alerts, cash balance) and provides navigation
 * buttons to all management screens accessible to administrators.
 * </p>
 */
public class AdminDashboardController {

    private static final Logger LOG = LogManager.getLogger(AdminDashboardController.class);

    // ---- Header labels -------------------------------------------------
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label cashBalanceLabel;
    @FXML
    private Label lowStockCountLabel;

    // ---- Low-stock table -----------------------------------------------
    @FXML
    private TableView<Good> lowStockTable;
    @FXML
    private TableColumn<Good, String> colGoodName;
    @FXML
    private TableColumn<Good, String> colCategory;
    @FXML
    private TableColumn<Good, Integer> colQty;
    @FXML
    private TableColumn<Good, Integer> colThreshold;

    // ---- Recent activity table -----------------------------------------
    @FXML
    private TableView<ActivityLog> activityTable;
    @FXML
    private TableColumn<ActivityLog, String> colLogDate;
    @FXML
    private TableColumn<ActivityLog, String> colLogUser;
    @FXML
    private TableColumn<ActivityLog, String> colLogAction;

    // ---- Services ------------------------------------------------------
    private final GoodService goodService = new GoodService();
    private final CashService cashService = new CashService();
    private final ActivityLogService logService = new ActivityLogService();

    // ---- Init ----------------------------------------------------------

    @FXML
    public void initialize() {
        // Welcome banner
        var user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + (user != null ? user.getFullName() : "Admin"));

        // Cash balance
        CashRegister reg = cashService.getDefault();
        if (reg != null) {
            cashBalanceLabel.setText("Cash: " + reg.getBalance() + " BGN");
            if (reg.isBelowThreshold()) {
                AlertUtil.showCashAlert(reg.getName(), reg.getBalance(), reg.getMinThreshold());
            }
        }

        // Low-stock table
        colGoodName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));

        List<Good> lowStock = goodService.findBelowThreshold();
        lowStockCountLabel.setText("Low-stock items: " + lowStock.size());
        lowStockTable.setItems(FXCollections.observableArrayList(lowStock));

        if (!lowStock.isEmpty()) {
            lowStock.forEach(g -> AlertUtil.showStockAlert(g.getName(), g.getQuantity(), g.getMinThreshold()));
        }

        // Recent activity
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("logDate"));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        activityTable.setItems(FXCollections.observableArrayList(logService.findRecent(20)));
    }

    // ---- Navigation buttons --------------------------------------------

    @FXML
    private void openUserManagement() {
        MainApp.loadScene("/fxml/user_management.fxml", "User Management", 900, 600);
    }

    @FXML
    private void openSupplierManagement() {
        MainApp.loadScene("/fxml/supplier_management.fxml", "Supplier Management", 900, 600);
    }

    @FXML
    private void openClientManagement() {
        MainApp.loadScene("/fxml/client_management.fxml", "Client Management", 900, 600);
    }

    @FXML
    private void openGoodManagement() {
        MainApp.loadScene("/fxml/good_management.fxml", "Goods / Nomenclature", 1000, 650);
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
