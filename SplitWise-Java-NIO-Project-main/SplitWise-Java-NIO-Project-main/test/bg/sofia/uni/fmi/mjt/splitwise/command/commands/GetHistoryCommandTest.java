package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetHistoryCommandTest {

    @Mock
    private DataRepository repository;
    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private GetHistoryCommand command;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        command = new GetHistoryCommand(repository, sessions);
    }

    @Test
    void testExecuteNotLoggedIn() {
        assertThrows(NotLoggedInException.class, () -> command.execute("", clientKey));
    }

    @Test
    void testExecuteEmptyHistory() throws NotLoggedInException {
        String username = "ivan";
        sessions.put(clientKey, username);

        when(repository.getTransactionHistory(username)).thenReturn(Collections.emptyList());

        String result = command.execute("", clientKey);

        assertEquals("You have no transaction history yet.", result);
    }

    @Test
    void testExecuteWithHistoryRecords() throws NotLoggedInException {
        String username = "ivan";
        sessions.put(clientKey, username);

        List<String> mockHistory = List.of(
            "[2023-10-01] Paid 10 LV to Peter",
            "[2023-10-02] Received 5 LV from Maria"
        );

        when(repository.getTransactionHistory(username)).thenReturn(mockHistory);

        String result = command.execute("", clientKey);

        assertTrue(result.contains("*** Payment History ***"));
        assertTrue(result.contains("Paid 10 LV to Peter"));
        assertTrue(result.contains("Received 5 LV from Maria"));
    }

    @Test
    void testExecuteIgnoresAdditionalArguments() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        when(repository.getTransactionHistory(username)).thenReturn(List.of("Payment 1"));

        String result = command.execute("some garbage arguments", clientKey);

        assertTrue(result.contains("Payment 1"));
        assertFalse(result.contains("garbage"));
    }

    @Test
    void testExecuteFormatsOutputCorrectlyWithoutTrailingNewLine() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        List<String> history = List.of("Line 1", "Line 2");
        when(repository.getTransactionHistory(username)).thenReturn(history);

        String result = command.execute("", clientKey);

        String expectedEnd = "Line 2";
        assertTrue(result.endsWith(expectedEnd), "Output should not end with a new line character due to strip()");

        assertTrue(result.contains("Line 1" + System.lineSeparator() + "Line 2"));
    }

    @Test
    void testExecuteCallsRepositoryWithCorrectUsername() throws NotLoggedInException {
        sessions.put(clientKey, "Dani");
        when(repository.getTransactionHistory("Dani")).thenReturn(Collections.emptyList());
        command.execute("", clientKey);

        verify(repository).getTransactionHistory("Dani");
    }

    @Test
    void testExecuteHandlesSpecialCharacters() throws NotLoggedInException {
        String username = "Ivan";
        sessions.put(clientKey, username);

        String specialMessage = "Paying for 🌯";
        when(repository.getTransactionHistory(username)).thenReturn(List.of(specialMessage));
        String result = command.execute("", clientKey);

        assertTrue(result.contains(specialMessage));
        assertTrue(result.contains("🌯"));
    }
}