package com.warehouse.service;

import com.warehouse.dao.UserDAO;
import com.warehouse.model.Role;
import com.warehouse.model.User;
import com.warehouse.util.PasswordUtil;
import com.warehouse.util.SessionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    private User activeAdmin;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        ActivityLogService logService = mock(ActivityLogService.class);
        userService = new UserService(userDAO, logService);

        activeAdmin = new User();
        activeAdmin.setId(1);
        activeAdmin.setUsername("admin");
        activeAdmin.setPassword(PasswordUtil.hash("admin123"));
        activeAdmin.setFullName("Admin User");
        activeAdmin.setEmail("admin@warehouse.com");
        activeAdmin.setRole(Role.ADMIN);
        activeAdmin.setActive(true);
        activeAdmin.setCreatedAt(LocalDateTime.now());

        inactiveUser = new User();
        inactiveUser.setId(2);
        inactiveUser.setUsername("inactive");
        inactiveUser.setPassword(PasswordUtil.hash("pass"));
        inactiveUser.setRole(Role.OPERATOR);
        inactiveUser.setActive(false);
    }

    @AfterEach
    void tearDown() {
        SessionManager.clearSession();
    }

    // ---- login tests ----

    @Test
    @DisplayName("Valid credentials → login succeeds and session is set")
    void login_validCredentials_setsSession() {
        when(userDAO.findByUsername("admin")).thenReturn(Optional.of(activeAdmin));

        boolean result = userService.login("admin", "admin123");

        assertTrue(result);
        assertTrue(SessionManager.isLoggedIn());
        assertEquals("admin", SessionManager.getCurrentUser().getUsername());
    }

    @Test
    @DisplayName("Wrong password → login fails")
    void login_wrongPassword_returnsFalse() {
        when(userDAO.findByUsername("admin")).thenReturn(Optional.of(activeAdmin));

        boolean result = userService.login("admin", "wrongpass");

        assertFalse(result);
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("Unknown username → login fails")
    void login_unknownUsername_returnsFalse() {
        when(userDAO.findByUsername("ghost")).thenReturn(Optional.empty());

        boolean result = userService.login("ghost", "any");

        assertFalse(result);
    }

    @Test
    @DisplayName("Inactive account → login rejected")
    void login_inactiveAccount_returnsFalse() {
        when(userDAO.findByUsername("inactive")).thenReturn(Optional.of(inactiveUser));

        boolean result = userService.login("inactive", "pass");

        assertFalse(result);
    }

    // ---- logout test ----

    @Test
    @DisplayName("logout() clears session")
    void logout_clearsSession() {
        SessionManager.setCurrentUser(activeAdmin);
        assertTrue(SessionManager.isLoggedIn());

        userService.logout();

        assertFalse(SessionManager.isLoggedIn());
    }

    // ---- createUser (admin guard) ----

    @Test
    @DisplayName("Non-admin cannot create user → SecurityException")
    void createUser_nonAdmin_throwsSecurityException() {
        User operator = new User();
        operator.setRole(Role.OPERATOR);
        operator.setActive(true);
        SessionManager.setCurrentUser(operator);

        assertThrows(SecurityException.class,
                () -> userService.createUser("newuser", "pass", "New User", "e@mail.com", Role.OPERATOR));
    }

    @Test
    @DisplayName("Admin can create a new user")
    void createUser_asAdmin_savesUser() {
        SessionManager.setCurrentUser(activeAdmin);

        userService.createUser("newuser", "pass123", "New User", "new@mail.com", Role.OPERATOR);

        verify(userDAO, times(1)).save(any(User.class));
    }
}
