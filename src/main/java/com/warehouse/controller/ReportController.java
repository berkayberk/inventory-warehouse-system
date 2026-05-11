package com.warehouse.controller;

import com.warehouse.MainApp;
import com.warehouse.model.*;
import com.warehouse.service.ReportService;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Reports / Inquiries screen.
 * All reports support a date-range filter.
 */
public class ReportController {

    private static final Logger LOG = LogManager.getLogger(ReportController.class);

    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;

    // ---- Supplies report tab
    @FXML
    private TableView<Invoice> suppliesTable;
    @FXML
    private TableColumn<Invoice, String> colSupDate;
    @FXML
    private TableColumn<Invoice, String> colSupNumber;
    @FXML
    private TableColumn<Invoice, String> colSupSupplier;
    @FXML
    private TableColumn<Invoice, String> colSupTotal;
    @FXML
    private TableColumn<Invoice, String> colSupOperator;
    @FXML
    private Label totalExpensesLabel;

    // ---- Sales report tab
    @FXML
    private TableView<Invoice> salesTable;
    @FXML
    private TableColumn<Invoice, String> colSaleDate;
    @FXML
    private TableColumn<Invoice, String> colSaleNumber;
    @FXML
    private TableColumn<Invoice, String> colSaleClient;
    @FXML
    private TableColumn<Invoice, String> colSaleTotal;
    @FXML
    private TableColumn<Invoice, String> colSaleOp;
    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label profitLabel;

    // ---- Stock report tab
    @FXML
    private TableView<Good> stockTable;
    @FXML
    private TableColumn<Good, String> colStockName;
    @FXML
    private TableColumn<Good, String> colStockCat;
    @FXML
    private TableColumn<Good, Integer> colStockQty;
    @FXML
    private TableColumn<Good, String> colStockPrice;
    @FXML
    private TableColumn<Good, String> colStockValue;
    @FXML
    private TableColumn<Good, String> colStockStatus;

    // ---- Cash movement tab
    @FXML
    private TableView<CashTransaction> cashTable;
    @FXML
    private TableColumn<CashTransaction, String> colCashDate;
    @FXML
    private TableColumn<CashTransaction, String> colCashType;
    @FXML
    private TableColumn<CashTransaction, String> colCashAmount;
    @FXML
    private TableColumn<CashTransaction, String> colCashDesc;
    @FXML
    private TableColumn<CashTransaction, String> colCashOp;

    // ---- Activity log tab
    @FXML
    private TableView<ActivityLog> logTable;
    @FXML
    private TableColumn<ActivityLog, String> colLogDate;
    @FXML
    private TableColumn<ActivityLog, String> colLogUser;
    @FXML
    private TableColumn<ActivityLog, String> colLogAction;
    @FXML
    private TableColumn<ActivityLog, String> colLogDetail;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        // Default date range: last 30 days
        fromDate.setValue(LocalDate.now().minusDays(30));
        toDate.setValue(LocalDate.now());

        // ---- Supplies columns
        colSupDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceDate().toString()));
        colSupNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colSupSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colSupTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotalAmount().toPlainString()));
        colSupOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

        // ---- Sales columns
        colSaleDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceDate().toString()));
        colSaleNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colSaleClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colSaleTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotalAmount().toPlainString()));
        colSaleOp.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

        // ---- Stock columns
        colStockName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStockCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStockQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStockPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSalesPrice().toPlainString()));
        colStockValue.setCellValueFactory(c -> {
            Good g = c.getValue();
            BigDecimal val = g.getSalesPrice().multiply(BigDecimal.valueOf(g.getQuantity()));
            return new SimpleStringProperty(val.toPlainString());
        });
        colStockStatus.setCellValueFactory(c -> {
            Good g = c.getValue();
            return new SimpleStringProperty(
                    g.isOutOfStock() ? "OUT OF STOCK" : g.isBelowThreshold() ? "LOW" : "OK");
        });

        // ---- Cash columns
        colCashDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionDate() != null
                ? c.getValue().getTransactionDate().toString()
                : ""));
        colCashType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        colCashAmount.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAmount().toPlainString()));
        colCashDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCashOp.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

        // ---- Log columns
        colLogDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogDate() != null
                ? c.getValue().getLogDate().toString()
                : ""));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colLogDetail.setCellValueFactory(new PropertyValueFactory<>("details"));

        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        LocalDate from = getFrom();
        LocalDate to = getTo();
        if (from == null || to == null) {
            AlertUtil.showWarning("Date", "Please select both From and To dates.");
            return;
        }

        // Supplies
        List<Invoice> supplies = reportService.suppliesReport(from, to);
        suppliesTable.setItems(FXCollections.observableArrayList(supplies));
        totalExpensesLabel.setText("Total Expenses: " + reportService.totalExpenses(from, to));

        // Sales
        List<Invoice> sales = reportService.salesReport(from, to);
        salesTable.setItems(FXCollections.observableArrayList(sales));
        totalIncomeLabel.setText("Total Income: " + reportService.totalIncome(from, to));
        profitLabel.setText("Profit: " + reportService.profitReport(from, to));

        // Stock (always current, ignoring dates)
        stockTable.setItems(FXCollections.observableArrayList(reportService.stockReport()));

        // Cash movement
        cashTable.setItems(FXCollections.observableArrayList(
                reportService.cashMovementReport(from, to)));

        // Activity log
        logTable.setItems(FXCollections.observableArrayList(
                reportService.operatorActivityReport(from, to)));
    }

    @FXML
    private void goBack() {
        boolean isAdmin = SessionManager.isAdmin();
        MainApp.loadScene(
                isAdmin ? "/fxml/admin_dashboard.fxml" : "/fxml/operator_dashboard.fxml",
                isAdmin ? "Admin Dashboard" : "Operator Dashboard", 1100, 700);
    }

    private LocalDate getFrom() {
        return fromDate.getValue();
    }

    private LocalDate getTo() {
        return toDate.getValue();
    }
}
