package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidAmountException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayedCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private PayedCommand payedCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        payedCommand = new PayedCommand(repository, sessions);
    }

    @Test
    void testExecuteSuccessfulPayment() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        double amount = 50.0;
        String arguments = amount + " " + debtor;
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 100.0);
        Alex.updateBalance(creditor, -100.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        String result = payedCommand.execute(arguments, clientKey);

        assertTrue(result.contains("payed you"));
        assertTrue(result.contains("50.00"));

        assertEquals(50.0, Ivan.getBalances().get(debtor), 0.01);
        assertEquals(-50.0, Alex.getBalances().get(creditor), 0.01);
        verify(repository, times(1)).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenNotLoggedIn() {
        String arguments = "50 Alex";

        NotLoggedInException exception = assertThrows(
            NotLoggedInException.class,
            () -> payedCommand.execute(arguments, clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenInvalidNumberOfArguments() {
        sessions.put(clientKey, "Ivan");
        String arguments = "50";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> payedCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: payed <amount> <username>", exception.getMessage());
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsNegative() {
        sessions.put(clientKey, "Ivan");
        String arguments = "-50 Alex";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> payedCommand.execute(arguments, clientKey)
        );

        assertEquals("Amount must be positive.", exception.getMessage());
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsZero() {
        sessions.put(clientKey, "Ivan");
        String arguments = "0 Alex";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> payedCommand.execute(arguments, clientKey)
        );

        assertEquals("Amount must be positive.", exception.getMessage());
    }

    @Test
    void testExecuteFullPaymentBalanceBecomesZero() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        double amount = 100.0;
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 100.0);
        Alex.updateBalance(creditor, -100.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        payedCommand.execute(amount + " " + debtor, clientKey);

        assertEquals(0.0, Ivan.getBalances().get(debtor), 0.01);
        assertEquals(0.0, Alex.getBalances().get(creditor), 0.01);
    }

    @Test
    void testExecutePartialPayment() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 100.0);
        Alex.updateBalance(creditor, -100.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        payedCommand.execute("25 Alex", clientKey);

        assertEquals(75.0, Ivan.getBalances().get(debtor), 0.01);
        assertEquals(-75.0, Alex.getBalances().get(creditor), 0.01);
    }

    @Test
    void testExecuteLogsTransaction() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        double amount = 50.0;
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 100.0);
        Alex.updateBalance(creditor, -100.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        payedCommand.execute(amount + " " + debtor, clientKey);

        verify(repository).logTransaction(eq(creditor), contains("Approved payment"));
    }

    @Test
    void testExecuteSmallAmount() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        double amount = 0.01;
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 10.0);
        Alex.updateBalance(creditor, -10.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        payedCommand.execute(amount + " " + debtor, clientKey);

        assertEquals(9.99, Ivan.getBalances().get(debtor), 0.01);
    }

    @Test
    void testExecuteLargeAmount() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        double amount = 1000000.0;
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 1000000.0);
        Alex.updateBalance(creditor, -1000000.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        payedCommand.execute(amount + " " + debtor, clientKey);

        assertEquals(0.0, Ivan.getBalances().get(debtor), 0.01);
    }

    @Test
    void testExecuteDecimalAmount() throws SplitWiseException {
        String creditor = "Ivan";
        String debtor = "Alex";
        String arguments = "123.45 Alex";
        sessions.put(clientKey, creditor);

        User Ivan = new User(creditor, "hash");
        User Alex = new User(debtor, "hash");

        Ivan.updateBalance(debtor, 200.0);
        Alex.updateBalance(creditor, -200.0);

        when(repository.getUser(creditor)).thenReturn(Ivan);
        when(repository.getUser(debtor)).thenReturn(Alex);
        when(repository.userExists(debtor)).thenReturn(true);

        String result = payedCommand.execute(arguments, clientKey);

        assertTrue(result.contains("123.45"));
        assertEquals(76.55, Ivan.getBalances().get(debtor), 0.01);
    }
}
