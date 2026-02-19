package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStatusCommandTest {

    @Mock
    private DataRepository repository;
    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private GetStatusCommand command;
    private User currentUser;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        command = new GetStatusCommand(repository, sessions);
        currentUser = new User("Ivan", "hash123");
    }

    @Test
    void testExecuteNotLoggedIn() {
        assertThrows(NotLoggedInException.class, () -> command.execute("", clientKey));
    }

    @Test
    void testExecuteNoFriendsNoGroups() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);
        when(repository.getAllGroups()).thenReturn(Collections.emptyList());

        String result = command.execute("", clientKey);

        assertTrue(result.contains("No debts with friends."));
        assertTrue(result.contains("No groups."));
    }

    @Test
    void testExecuteFriendOwesMe() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);
        when(repository.getAllGroups()).thenReturn(Collections.emptyList());

        currentUser.addFriend("Alex");
        currentUser.updateBalance("Alex", 20.50);

        String result = command.execute("", clientKey);

        assertTrue(result.contains("Alex: Owes you 20.50 LV"));
        assertFalse(result.contains("You owe"));
    }

    @Test
    void testExecuteIOweFriend() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);
        when(repository.getAllGroups()).thenReturn(Collections.emptyList());

        currentUser.addFriend("Dani");
        currentUser.updateBalance("Dani", -15.00);

        String result = command.execute("", clientKey);

        assertTrue(result.contains("Dani: You owe 15.00 LV"));
    }

    @Test
    void testExecuteIgnoreInsignificantAmount() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);
        when(repository.getAllGroups()).thenReturn(Collections.emptyList());

        currentUser.addFriend("Dani");
        currentUser.updateBalance("Dani", 0.004);

        String result = command.execute("", clientKey);

        assertFalse(result.contains("Dani"));
        assertTrue(result.contains("No debts with friends."));
    }

    @Test
    void testExecuteWithGroups() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);

        Group group = new Group("Trip", Set.of("Ivan", "Alex"));
        when(repository.getAllGroups()).thenReturn(List.of(group));

        currentUser.updateBalance("Alex", 50.00);

        String result = command.execute("", clientKey);

        assertTrue(result.contains("Groups:"));
        assertTrue(result.contains("* Trip"));
        assertTrue(result.contains("Alex: Owes you 50.00 LV"));
    }

    @Test
    void testExecuteWithGroupWhereNotMember() throws NotLoggedInException {
        sessions.put(clientKey, "Ivan");
        when(repository.getUser("Ivan")).thenReturn(currentUser);

        Group otherGroup = new Group("Secret", Set.of("Alex", "Dani"));
        when(repository.getAllGroups()).thenReturn(List.of(otherGroup));

        String result = command.execute("", clientKey);

        assertFalse(result.contains("Secret"));
        assertTrue(result.contains("No groups."));
    }
}