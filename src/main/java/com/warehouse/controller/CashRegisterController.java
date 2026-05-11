package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.CashRegister;
import com.warehouse.model.CashTransaction;
import com.warehouse.service.CashService;
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
 * Shows the cash register balance and allows admin deposit / withdrawal.
 */
public class CashRegisterController {

    private static final Logger LOG = LogManager.getLogger(CashRegisterController.class);

    @FXML
    private Label balanceLabel;
    @FXML
    private Label thresholdLabel;

    @FXML
    private TextField amountField;
    @FXML
    private TextField descriptionField;

    @FXML
    private TableView<CashTransaction> transactionTable;
    @FXML
    private TableColumn<CashTransaction, String> colTxDate;
    @FXML
    private TableColumn<CashTransaction, String> colTxType;
    @FXML
    private TableColumn<CashTransaction, String> colTxAmount;
    @FXML
    private TableColumn<CashTransaction, String> colTxDesc;
    @FXML
    private TableColumn<CashTransaction, String> colTxOperator;

    private final CashService cashService = new CashService();
    private CashRegister currentRegister;

    @FXML
    public void initialize() {
        currentRegister = cashService.getDefault();
        refreshHeader();

        colTxDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionDate() != null
                ? c.getValue().getTransactionDate().toString()
                : ""));
        colTxType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        colTxAmount.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAmount().toPlainString()));
        colTxDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colTxOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

        refreshTransactions();
    }

    @FXML
    private void handleDeposit() {
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(amountField.getText().trim());
            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive.");
            cashService.deposit(currentRegister.getId(), amount, descriptionField.getText().trim());
            amountField.clear();
            descriptionField.clear();
            currentRegister = cashService.getDefault();
            refreshHeader();
            refreshTransactions();
            AlertUtil.showInfo("Deposit", "Deposit of " + amount + " recorded.");
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    @FXML
    private void handleWithdraw() {
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(amountField.getText().trim());
            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive.");
            cashService.withdraw(currentRegister.getId(), amount, descriptionField.getText().trim());
            amountField.clear();
            descriptionField.clear();
            currentRegister = cashService.getDefault();
            refreshHeader();
            refreshTransactions();
            AlertUtil.showInfo("Withdrawal", "Withdrawal of " + amount + " recorded.");
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

    private void refreshHeader() {
        if (currentRegister != null) {
            balanceLabel.setText("Balance: " + currentRegister.getBalance() + " BGN");
            thresholdLabel.setText("Min threshold: " + currentRegister.getMinThreshold() + " BGN");
            if (currentRegister.isBelowThreshold()) {
                AlertUtil.showCashAlert(currentRegister.getName(),
                        currentRegister.getBalance(), currentRegister.getMinThreshold());
            }
        }
    }

    private void refreshTransactions() {
        if (currentRegister != null) {
            transactionTable.setItems(FXCollections.observableArrayList(
                    cashService.getTransactions(currentRegister.getId())));
        }
    }
}
