package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.AlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private LoginCommand loginCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        loginCommand = new LoginCommand(repository, sessions);
    }

    @Test
    void testExecuteSuccessfulLoginNoNotifications() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        when(repository.getUser(username)).thenReturn(mockUser);

        String result = loginCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Successful login!"));
        assertTrue(result.contains("No notifications to show."));
        assertEquals(username, sessions.get(clientKey));
        verify(repository, times(1)).getUser(username);
    }

    @Test
    void testExecuteSuccessfulLoginWithNotifications() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        mockUser.addNotification("Ivan owes you 50 LV");
        mockUser.addNotification("You owe Alex 25 LV");

        when(repository.getUser(username)).thenReturn(mockUser);

        String result = loginCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Successful login!"));
        assertTrue(result.contains("*** Notifications ***"));
        assertTrue(result.contains("Ivan owes you 50 LV"));
        assertTrue(result.contains("You owe Alex 25 LV"));
        assertEquals(username, sessions.get(clientKey));
        verify(repository, times(1)).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenInvalidNumberOfArguments() {
        String arguments = "onlyusername";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> loginCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: login <username> <password>", exception.getMessage());
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteNotificationsClearedAfterLogin() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        mockUser.addNotification("Test notification");

        when(repository.getUser(username)).thenReturn(mockUser);

        loginCommand.execute(arguments, clientKey);

        List<String> notifications = mockUser.getAndClearNotifications();
        assertTrue(notifications.isEmpty(), "Notifications should be cleared after login");
        verify(repository, times(1)).saveData();
    }

    @Test
    void testExecuteSessionCreatedCorrectly() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        when(repository.getUser(username)).thenReturn(mockUser);

        loginCommand.execute(arguments, clientKey);

        assertTrue(sessions.containsKey(clientKey), "Session should be created");
        assertEquals(username, sessions.get(clientKey), "Session should map to correct username");
    }

    @Test
    void testExecuteEmptyArguments() {
        String arguments = "";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> loginCommand.execute(arguments, clientKey)
        );

        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteOnlySpaces() {
        String arguments = "   ";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> loginCommand.execute(arguments, clientKey)
        );

        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteCaseSensitiveUsername() {
        String arguments = "TestUser password123";

        when(repository.getUser("TestUser")).thenReturn(null);

        assertThrows(
            InvalidCredentialsException.class,
            () -> loginCommand.execute(arguments, clientKey)
        );
    }

    @Test
    void testExecuteMultipleNotifications() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        mockUser.addNotification("Notification 1");
        mockUser.addNotification("Notification 2");
        mockUser.addNotification("Notification 3");

        when(repository.getUser(username)).thenReturn(mockUser);

        String result = loginCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Notification 1"));
        assertTrue(result.contains("Notification 2"));
        assertTrue(result.contains("Notification 3"));
    }

    @Test
    void testExecuteLongPassword() throws SplitWiseException {
        String username = "testuser";
        String password = "a".repeat(100);
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        when(repository.getUser(username)).thenReturn(mockUser);

        String result = loginCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Successful login!"));
        assertEquals(username, sessions.get(clientKey));
    }

    @Test
    void testExecuteSpecialCharactersInPassword() throws SplitWiseException {
        String username = "testuser";
        String password = "p@ss#w0rd!";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);
        when(repository.getUser(username)).thenReturn(mockUser);

        String result = loginCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Successful login!"));
    }

    @Test
    void testExecuteDataSavedOnlyWhenNotificationsExist() throws SplitWiseException {
        String username = "testuser";
        String password = "password123";
        String passwordHash = String.valueOf(password.hashCode());
        String arguments = username + " " + password;

        User mockUser = new User(username, passwordHash);

        when(repository.getUser(username)).thenReturn(mockUser);

        loginCommand.execute(arguments, clientKey);

        verify(repository, never()).saveData();
    }
}
