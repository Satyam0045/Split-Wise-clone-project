package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogoutCommandTest {

    @Mock
    private SelectionKey clientKey;

    @Mock
    private SelectionKey anotherClientKey;

    private Map<SelectionKey, String> sessions;
    private LogoutCommand logoutCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        logoutCommand = new LogoutCommand(sessions);
    }

    @Test
    void testExecuteSuccessfulLogout() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertEquals("User Ivan logged out successfully.", result);
        assertFalse(sessions.containsKey(clientKey),
            "Session should be removed after logout");
    }

    @Test
    void testExecuteThrowsExceptionWhenNotLoggedIn() {
        NotLoggedInException exception = assertThrows(
            NotLoggedInException.class,
            () -> logoutCommand.execute("", clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
    }

    @Test
    void testExecuteThrowsExceptionWhenSessionDoesNotExist() {
        sessions.put(anotherClientKey, "otheruser");

        NotLoggedInException exception = assertThrows(
            NotLoggedInException.class,
            () -> logoutCommand.execute("", clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
        assertTrue(sessions.containsKey(anotherClientKey),
            "Other sessions should not be affected");
    }

    @Test
    void testExecuteReturnsCorrectUsername() throws NotLoggedInException {
        String username = "Dani";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertTrue(result.contains(username),
            "Result should contain the username that logged out");
        assertTrue(result.contains("logged out successfully"),
            "Result should indicate successful logout");
    }

    @Test
    void testExecuteIgnoresArguments() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("some random arguments", clientKey);

        assertEquals("User Ivan logged out successfully.", result);
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteMultipleLogouts() throws NotLoggedInException {
        String user1 = "Ivan";
        String user2 = "Alex";
        String user3 = "Dani";

        SelectionKey key1 = clientKey;
        SelectionKey key2 = anotherClientKey;
        SelectionKey key3 = org.mockito.Mockito.mock(SelectionKey.class);

        sessions.put(key1, user1);
        sessions.put(key2, user2);
        sessions.put(key3, user3);

        assertEquals(3, sessions.size());

        logoutCommand.execute("", key1);
        assertEquals(2, sessions.size());
        assertFalse(sessions.containsKey(key1));

        logoutCommand.execute("", key2);
        assertEquals(1, sessions.size());
        assertFalse(sessions.containsKey(key2));

        logoutCommand.execute("", key3);
        assertEquals(0, sessions.size());
        assertFalse(sessions.containsKey(key3));
    }

    @Test
    void testExecuteEmptyArguments() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertTrue(result.contains("Ivan"));
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteNullArguments() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute(null, clientKey);

        assertTrue(result.contains("Ivan"));
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteWhitespaceArguments() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("   ", clientKey);

        assertTrue(result.contains("Ivan"));
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteLongUsername() throws NotLoggedInException {
        String username = "a".repeat(100);
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertTrue(result.contains(username));
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteUsernameWithSpecialCharacters() throws NotLoggedInException {
        String username = "user_name_123";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertTrue(result.contains(username));
        assertFalse(sessions.containsKey(clientKey));
    }

    @Test
    void testExecuteNumericUsername() throws NotLoggedInException {
        String username = "123456";
        sessions.put(clientKey, username);

        String result = logoutCommand.execute("", clientKey);

        assertTrue(result.contains(username));
        assertFalse(sessions.containsKey(clientKey));
    }
}
