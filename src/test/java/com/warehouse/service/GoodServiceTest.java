package com.warehouse.service;

import com.warehouse.dao.GoodDAO;
import com.warehouse.model.Good;
import com.warehouse.model.Role;
import com.warehouse.model.User;
import com.warehouse.util.SessionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodServiceTest {

    @Mock
    private GoodDAO goodDAO;

    private GoodService goodService;

    private User adminUser;
    private User operatorUser;

    @BeforeEach
    void setUp() {
        ActivityLogService logService = mock(ActivityLogService.class);
        goodService = new GoodService(goodDAO, logService);

        adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        operatorUser = new User();
        operatorUser.setId(2);
        operatorUser.setUsername("oper1");
        operatorUser.setRole(Role.OPERATOR);
        operatorUser.setActive(true);
    }

    @AfterEach
    void tearDown() {
        SessionManager.clearSession();
    }

    // ---- adjustStock ----

    @Test
    @DisplayName("adjustStock with sufficient quantity → succeeds")
    void adjustStock_sufficientQty_callsDAO() {
        Good good = buildGood(10);
        when(goodDAO.findById(1)).thenReturn(Optional.of(good));

        goodService.adjustStock(1, -5); // reduce by 5 (simulate sale)

        verify(goodDAO, times(1)).adjustQuantity(1, -5);
    }

    @Test
    @DisplayName("adjustStock with insufficient quantity → throws IllegalStateException")
    void adjustStock_insufficientQty_throwsException() {
        Good good = buildGood(3);
        when(goodDAO.findById(1)).thenReturn(Optional.of(good));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> goodService.adjustStock(1, -10));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(goodDAO, never()).adjustQuantity(anyInt(), anyInt());
    }

    @Test
    @DisplayName("adjustStock on non-existent good → throws IllegalArgumentException")
    void adjustStock_goodNotFound_throwsException() {
        when(goodDAO.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> goodService.adjustStock(99, -1));
    }

    @Test
    @DisplayName("adjustStock with positive delta (purchase) → always allowed")
    void adjustStock_positive_alwaysSucceeds() {
        Good good = buildGood(0);
        when(goodDAO.findById(1)).thenReturn(Optional.of(good));

        goodService.adjustStock(1, 20);

        verify(goodDAO, times(1)).adjustQuantity(1, 20);
    }

    // ---- createGood (admin guard) ----

    @Test
    @DisplayName("Operator cannot create good → SecurityException")
    void createGood_asOperator_throwsSecurityException() {
        SessionManager.setCurrentUser(operatorUser);

        assertThrows(SecurityException.class, () -> goodService.createGood("ProdX", "Cat", "pcs",
                new BigDecimal("5.00"), new BigDecimal("9.00"), 0, 5));
    }

    @Test
    @DisplayName("Admin can create a good")
    void createGood_asAdmin_savesGood() {
        SessionManager.setCurrentUser(adminUser);

        goodService.createGood("ProdX", "Cat", "pcs",
                new BigDecimal("5.00"), new BigDecimal("9.00"), 0, 5);

        verify(goodDAO, times(1)).save(any(Good.class));
    }

    // ---- helper ----

    private Good buildGood(int qty) {
        Good g = new Good();
        g.setId(1);
        g.setName("Test Product");
        g.setCategory("Test");
        g.setUnit("pcs");
        g.setDeliveryPrice(new BigDecimal("5.00"));
        g.setSalesPrice(new BigDecimal("9.00"));
        g.setQuantity(qty);
        g.setMinThreshold(2);
        g.setActive(true);
        return g;
    }
}
