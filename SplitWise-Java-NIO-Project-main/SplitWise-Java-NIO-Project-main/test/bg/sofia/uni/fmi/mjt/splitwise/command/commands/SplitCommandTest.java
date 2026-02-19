package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidAmountException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserNotFoundException;
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
class SplitCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private SplitCommand splitCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        splitCommand = new SplitCommand(repository, sessions);
    }

    @Test
    void testExecuteSuccessfulSplit() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        double amount = 100.0;
        String reason = "dinner";
        String arguments = amount + " " + friendUser + " " + reason;

        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        String result = splitCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Splitted 100.00 LV"));
        assertEquals(50.0, Ivan.getBalances().get(friendUser), 0.01);
        assertEquals(-50.0, Alex.getBalances().get(currentUser), 0.01);
        verify(repository, times(1)).saveData();
        verify(repository, times(1)).logTransaction(eq(currentUser), anyString());
    }

    @Test
    void testExecuteThrowsExceptionWhenNotLoggedIn() {
        String arguments = "100 Alex dinner";

        NotLoggedInException exception = assertThrows(
            NotLoggedInException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenInvalidNumberOfArguments() {
        sessions.put(clientKey, "Ivan");
        String arguments = "100 Alex";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: split <amount> <username> <reason>", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsInvalid() {
        sessions.put(clientKey, "Ivan");
        String arguments = "notanumber Alex dinner";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Invalid amount", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsNegative() {
        sessions.put(clientKey, "Ivan");
        String arguments = "-50 Alex dinner";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Amount must be positive.", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsZero() {
        sessions.put(clientKey, "Ivan");
        String arguments = "0 Alex dinner";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Amount must be positive.", exception.getMessage());
        verify(repository, never()).saveData();
    }


    @Test
    void testExecuteThrowsExceptionWhenFriendDoesNotExist() {
        String currentUser = "Ivan";
        sessions.put(clientKey, currentUser);
        String arguments = "100 nonexistent dinner";

        User Ivan = new User(currentUser, "hash");
        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser("nonexistent")).thenReturn(null);

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertTrue(exception.getMessage().contains("nonexistent"));
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenUserNotInFriendList() {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);
        String arguments = "100 Alex dinner";

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> splitCommand.execute(arguments, clientKey)
        );

        assertEquals("Current user is not in your friend list. Add them first.",
            exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteSplitsAmountInHalf() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        double amount = 50.0;
        sessions.put(clientKey, currentUser);
        String arguments = amount + " " + friendUser + " pizza";

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        splitCommand.execute(arguments, clientKey);

        assertEquals(25.0, Ivan.getBalances().get(friendUser), 0.01);
        assertEquals(-25.0, Alex.getBalances().get(currentUser), 0.01);
    }

    @Test
    void testExecuteAccumulatesBalanceCorrectly() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);


        Ivan.updateBalance(friendUser, 30.0);
        Alex.updateBalance(currentUser, -30.0);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        splitCommand.execute("100 Alex lunch", clientKey);

        assertEquals(80.0, Ivan.getBalances().get(friendUser), 0.01);
        assertEquals(-80.0, Alex.getBalances().get(currentUser), 0.01);
    }

    @Test
    void testExecuteHandleOddAmounts() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        double amount = 99.99;
        sessions.put(clientKey, currentUser);
        String arguments = amount + " " + friendUser + " taxi";

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        splitCommand.execute(arguments, clientKey);

        assertEquals(49.995, Ivan.getBalances().get(friendUser), 0.001);
        assertEquals(-49.995, Alex.getBalances().get(currentUser), 0.001);
    }

    @Test
    void testExecuteSmallAmount() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        double amount = 0.01;
        sessions.put(clientKey, currentUser);
        String arguments = amount + " " + friendUser + " coffee";

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        String result = splitCommand.execute(arguments, clientKey);

        assertTrue(result.contains("0.01"));
        assertEquals(0.005, Ivan.getBalances().get(friendUser), 0.0001);
    }

    @Test
    void testExecuteLargeAmount() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        double amount = 1000000.0;
        sessions.put(clientKey, currentUser);
        String arguments = amount + " " + friendUser + " car";

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");
        Ivan.addFriend(friendUser);

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);

        String result = splitCommand.execute(arguments, clientKey);

        assertEquals(500000.0, Ivan.getBalances().get(friendUser), 0.01);
        assertEquals(-500000.0, Alex.getBalances().get(currentUser), 0.01);
    }
}
