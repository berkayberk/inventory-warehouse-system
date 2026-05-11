package com.warehouse.service;

import com.warehouse.dao.*;
import com.warehouse.model.*;
import com.warehouse.util.SessionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceDAO invoiceDAO;
    @Mock
    private GoodDAO goodDAO;
    @Mock
    private CashRegisterDAO cashRegisterDAO;
    @Mock
    private CashTransactionDAO cashTransactionDAO;
    @Mock
    private ActivityLogService logService;

    private InvoiceService invoiceService;
    private GoodService goodService;

    private User operator;
    private Good apple;
    private Good banana;
    private CashRegister register;

    @BeforeEach
    void setUp() {
        goodService = new GoodService(goodDAO, logService);
        CashService cashService = new CashService(cashRegisterDAO, cashTransactionDAO, logService);
        invoiceService = new InvoiceService(invoiceDAO, goodService, cashService, logService);

        operator = new User();
        operator.setId(5);
        operator.setUsername("oper1");
        operator.setRole(Role.OPERATOR);
        operator.setActive(true);
        SessionManager.setCurrentUser(operator);

        apple = buildGood(1, "Apple", new BigDecimal("0.50"), new BigDecimal("1.00"), 100);
        banana = buildGood(2, "Banana", new BigDecimal("0.30"), new BigDecimal("0.70"), 50);

        register = new CashRegister();
        register.setId(1);
        register.setBalance(new BigDecimal("500.00"));
        register.setMinThreshold(new BigDecimal("50.00"));
    }

    @AfterEach
    void tearDown() {
        SessionManager.clearSession();
    }

    // ---- createPurchase ----

    @Test
    @DisplayName("createPurchase → saves invoice, items, adjusts stock, records expense")
    void createPurchase_validData_completesSuccessfully() {
        when(invoiceDAO.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(101);
            return i;
        });
        when(goodDAO.findById(1)).thenReturn(Optional.of(apple));
        when(cashRegisterDAO.findDefault()).thenReturn(Optional.of(register));
        when(cashRegisterDAO.findById(1)).thenReturn(Optional.of(register));

        List<InvoiceItem> items = List.of(
                buildItem(1, 10, new BigDecimal("0.50")));

        assertDoesNotThrow(() -> invoiceService.createPurchase(3, "Test purchase", items));

        verify(invoiceDAO, times(1)).save(any(Invoice.class));
        verify(invoiceDAO, times(1)).saveItem(any(InvoiceItem.class));
        verify(goodDAO, times(1)).adjustQuantity(1, 10);
        verify(cashTransactionDAO, times(1)).save(any(CashTransaction.class));
    }

    // ---- createSale ----

    @Test
    @DisplayName("createSale with sufficient stock → succeeds")
    void createSale_sufficientStock_completesSuccessfully() {
        when(goodDAO.findById(1)).thenReturn(Optional.of(apple));
        when(invoiceDAO.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(202);
            return i;
        });
        when(cashRegisterDAO.findDefault()).thenReturn(Optional.of(register));
        when(cashRegisterDAO.findById(1)).thenReturn(Optional.of(register));

        List<InvoiceItem> items = List.of(
                buildItem(1, 5, new BigDecimal("1.00")));

        assertDoesNotThrow(() -> invoiceService.createSale(7, "Test sale", items));

        verify(goodDAO, times(1)).adjustQuantity(1, -5);
        verify(cashTransactionDAO, times(1)).save(any(CashTransaction.class));
    }

    @Test
    @DisplayName("createSale with insufficient stock → throws exception, nothing persisted")
    void createSale_insufficientStock_throwsAndRollsBack() {
        // apple only has 100 in stock; request 200
        when(goodDAO.findById(1)).thenReturn(Optional.of(apple));

        List<InvoiceItem> items = List.of(
                buildItem(1, 200, new BigDecimal("1.00")));

        assertThrows(IllegalStateException.class, () -> invoiceService.createSale(7, "Over-sell", items));

        // invoice must NOT have been saved
        verify(invoiceDAO, never()).save(any(Invoice.class));
        verify(goodDAO, never()).adjustQuantity(anyInt(), anyInt());
    }

    @Test
    @DisplayName("createSale with multiple items – first ok, second insufficient → nothing persisted")
    void createSale_secondItemInsufficient_allRolledBack() {
        when(goodDAO.findById(1)).thenReturn(Optional.of(apple)); // 100 in stock
        when(goodDAO.findById(2)).thenReturn(Optional.of(banana)); // 50 in stock

        List<InvoiceItem> items = List.of(
                buildItem(1, 10, new BigDecimal("1.00")), // ok
                buildItem(2, 100, new BigDecimal("0.70")) // too many
        );

        assertThrows(IllegalStateException.class, () -> invoiceService.createSale(7, "Mixed", items));

        verify(invoiceDAO, never()).save(any(Invoice.class));
    }

    // ---- helpers ----

    private Good buildGood(int id, String name, BigDecimal cost, BigDecimal price, int qty) {
        Good g = new Good();
        g.setId(id);
        g.setName(name);
        g.setCategory("Fruits");
        g.setUnit("kg");
        g.setDeliveryPrice(cost);
        g.setSalesPrice(price);
        g.setQuantity(qty);
        g.setMinThreshold(5);
        g.setActive(true);
        return g;
    }

    private InvoiceItem buildItem(int goodId, int qty, BigDecimal unitPrice) {
        InvoiceItem item = new InvoiceItem();
        item.setGoodId(goodId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        return item;
    }
}
