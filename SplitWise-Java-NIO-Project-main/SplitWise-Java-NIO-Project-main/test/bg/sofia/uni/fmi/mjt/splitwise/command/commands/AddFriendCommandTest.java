package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.FriendshipAlreadyExistsException;
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
class AddFriendCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private AddFriendCommand addFriendCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        addFriendCommand = new AddFriendCommand(repository, sessions);
    }

    @Test
    void testExecuteSuccessfullyAddsFriend() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);
        when(repository.userExists(friendUser)).thenReturn(true);

        String result = addFriendCommand.execute(friendUser, clientKey);

        assertEquals("Added Alex to your friend list.", result);
        assertTrue(Ivan.getFriends().contains(friendUser));
        assertTrue(Alex.getFriends().contains(currentUser));
        assertEquals(0.0, Ivan.getBalances().get(friendUser), 0.01);
        verify(repository, times(1)).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenNotLoggedIn() {
        String friendUser = "Alex";

        NotLoggedInException exception = assertThrows(
            NotLoggedInException.class,
            () -> addFriendCommand.execute(friendUser, clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenEmptyArgument() {
        sessions.put(clientKey, "Ivan");
        String arguments = "";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> addFriendCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: add-friend <username>", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenOnlySpaces() {
        sessions.put(clientKey, "Ivan");
        String arguments = "   ";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> addFriendCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: add-friend <username>", exception.getMessage());
        verify(repository, never()).saveData();
    }


    @Test
    void testExecuteInitializesBalanceToZero() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);
        when(repository.userExists(friendUser)).thenReturn(true);

        addFriendCommand.execute(friendUser, clientKey);

        assertTrue(Ivan.getBalances().containsKey(friendUser),
            "Balance should be initialized for friend");
        assertEquals(0.0, Ivan.getBalances().get(friendUser), 0.01,
            "Initial balance should be zero");
        assertTrue(Alex.getBalances().containsKey(currentUser),
            "Balance should be initialized for current user");
        assertEquals(0.0, Alex.getBalances().get(currentUser), 0.01,
            "Initial balance should be zero");
    }

    @Test
    void testExecuteTrimsWhitespace() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "  Alex  ";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User("Alex", "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser("Alex")).thenReturn(Alex);
        when(repository.userExists("Alex")).thenReturn(true);

        String result = addFriendCommand.execute(friendUser, clientKey);

        assertTrue(result.contains("Alex"));
        assertTrue(Ivan.getFriends().contains("Alex"));
    }

    @Test
    void testExecuteAddMultipleFriends() throws SplitWiseException {
        String currentUser = "Ivan";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User("Alex", "hash");
        User Dani = new User("Dani", "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser("Alex")).thenReturn(Alex);
        when(repository.getUser("Dani")).thenReturn(Dani);
        when(repository.userExists("Alex")).thenReturn(true);
        when(repository.userExists("Dani")).thenReturn(true);

        addFriendCommand.execute("Alex", clientKey);
        addFriendCommand.execute("Dani", clientKey);

        assertEquals(2, Ivan.getFriends().size());
        assertTrue(Ivan.getFriends().contains("Alex"));
        assertTrue(Ivan.getFriends().contains("Dani"));
        verify(repository, times(2)).saveData();
    }

    @Test
    void testExecuteCaseSensitiveUsername() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User("Alex", "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser("Alex")).thenReturn(Alex);
        when(repository.userExists("Alex")).thenReturn(true);

        addFriendCommand.execute(friendUser, clientKey);

        assertTrue(Ivan.getFriends().contains("Alex"));
    }

    @Test
    void testExecuteUsernameWithUnderscore() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex_Petrov";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);
        when(repository.userExists(friendUser)).thenReturn(true);

        String result = addFriendCommand.execute(friendUser, clientKey);

        assertTrue(Ivan.getFriends().contains("Alex_Petrov"));
    }

    @Test
    void testExecuteSavesDataAfterSuccess() throws SplitWiseException {
        String currentUser = "Ivan";
        String friendUser = "Alex";
        sessions.put(clientKey, currentUser);

        User Ivan = new User(currentUser, "hash");
        User Alex = new User(friendUser, "hash");

        when(repository.getUser(currentUser)).thenReturn(Ivan);
        when(repository.getUser(friendUser)).thenReturn(Alex);
        when(repository.userExists(friendUser)).thenReturn(true);

        addFriendCommand.execute(friendUser, clientKey);

        verify(repository, times(1)).saveData();
    }

}
