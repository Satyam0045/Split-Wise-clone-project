package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.*;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
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
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplitGroupCommandTest {

    private static final String IVAN = "Ivan";
    private static final String ALEX = "Alex";
    private static final String DANI = "Dani";
    private static final String GROUP_ROOMMATES = "roommates";
    private static final String HASH = "hash";

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private SplitGroupCommand splitGroupCommand;

    private User userIvan;
    private User userAlex;
    private User userDani;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        splitGroupCommand = new SplitGroupCommand(repository, sessions);

        userIvan = new User(IVAN, HASH);
        userAlex = new User(ALEX, HASH);
        userDani = new User(DANI, HASH);
    }

    private void setupStandardGroupAndUsers() {
        Set<String> members = Set.of(IVAN, ALEX, DANI);
        Group group = new Group(GROUP_ROOMMATES, members);

        when(repository.getGroup(GROUP_ROOMMATES)).thenReturn(group);
        when(repository.getUser(IVAN)).thenReturn(userIvan);
        when(repository.getUser(ALEX)).thenReturn(userAlex);
        when(repository.getUser(DANI)).thenReturn(userDani);
    }

    @Test
    void testExecuteSuccessfulSplit() throws SplitWiseException {
        sessions.put(clientKey, IVAN);
        setupStandardGroupAndUsers();

        String arguments = "300.0 " + GROUP_ROOMMATES + " groceries";

        String result = splitGroupCommand.execute(arguments, clientKey);

        assertTrue(result.contains("Split 300.00 LV"));
        assertTrue(result.contains(GROUP_ROOMMATES));


        assertEquals(100.0, userIvan.getBalances().get(ALEX), 0.01);
        assertEquals(100.0, userIvan.getBalances().get(DANI), 0.01);
        assertEquals(-100.0, userAlex.getBalances().get(IVAN), 0.01);
        assertEquals(-100.0, userDani.getBalances().get(IVAN), 0.01);

        verify(repository, times(1)).saveData();
        verify(repository, times(1)).logTransaction(eq(IVAN), anyString());
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsInvalid() {
        sessions.put(clientKey, IVAN);
        String arguments = "notanumber " + GROUP_ROOMMATES + " dinner";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> splitGroupCommand.execute(arguments, clientKey)
        );

        assertEquals("Invalid amount.", exception.getMessage());
        verify(repository, never()).saveData();
    }

    @Test
    void testExecuteThrowsExceptionWhenAmountIsNegative() {
        sessions.put(clientKey, IVAN);
        String arguments = "-100 " + GROUP_ROOMMATES + " dinner";

        InvalidAmountException exception = assertThrows(
            InvalidAmountException.class,
            () -> splitGroupCommand.execute(arguments, clientKey)
        );

        assertEquals("Amount must be positive.", exception.getMessage());
    }

    @Test
    void testExecuteThrowsExceptionWhenGroupDoesNotExist() {
        sessions.put(clientKey, IVAN);
        String arguments = "100 nonexistent dinner";

        when(repository.getGroup("nonexistent")).thenReturn(null);

        GroupNotFoundException exception = assertThrows(
            GroupNotFoundException.class,
            () -> splitGroupCommand.execute(arguments, clientKey)
        );

        assertEquals("Group is not found", exception.getMessage());
    }

    @Test
    void testExecuteThrowsExceptionWhenUserNotInGroup() {
        sessions.put(clientKey, IVAN);
        String arguments = "100 " + GROUP_ROOMMATES + " dinner";

        Group group = new Group(GROUP_ROOMMATES, Set.of(ALEX, DANI, "Dave"));
        when(repository.getGroup(GROUP_ROOMMATES)).thenReturn(group);

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> splitGroupCommand.execute(arguments, clientKey)
        );

        assertEquals("Current user is NOT a member of the group.", exception.getMessage());
    }

    @Test
    void testExecuteSplitsEquallyAmongAllMembers() throws SplitWiseException {
        sessions.put(clientKey, IVAN);

        User userDave = new User("Dave", HASH);
        Set<String> members = Set.of(IVAN, ALEX, DANI, "Dave");
        Group group = new Group(GROUP_ROOMMATES, members);

        when(repository.getGroup(GROUP_ROOMMATES)).thenReturn(group);
        when(repository.getUser(IVAN)).thenReturn(userIvan);
        when(repository.getUser(ALEX)).thenReturn(userAlex);
        when(repository.getUser(DANI)).thenReturn(userDani);
        when(repository.getUser("Dave")).thenReturn(userDave);

        splitGroupCommand.execute("400 " + GROUP_ROOMMATES + " utilities", clientKey);

        assertEquals(100.0, userIvan.getBalances().get(ALEX), 0.01);
        assertEquals(100.0, userIvan.getBalances().get(DANI), 0.01);
        assertEquals(100.0, userIvan.getBalances().get("Dave"), 0.01);
    }

    @Test
    void testExecuteAccumulatesBalancesCorrectly() throws SplitWiseException {
        sessions.put(clientKey, IVAN);
        setupStandardGroupAndUsers();

        userIvan.updateBalance(ALEX, 50.0);
        userAlex.updateBalance(IVAN, -50.0);

        splitGroupCommand.execute("300 " + GROUP_ROOMMATES + " dinner", clientKey);

        assertEquals(150.0, userIvan.getBalances().get(ALEX), 0.01);
        assertEquals(-150.0, userAlex.getBalances().get(IVAN), 0.01);
    }

    @Test
    void testExecuteLargeGroup() throws SplitWiseException {
        sessions.put(clientKey, IVAN);

        Set<String> members = Set.of(IVAN, ALEX, DANI, "Dave", "Ani", "Gogo");
        Group group = new Group("biggroup", members);

        User userDave = new User("Dave", HASH);
        User userAni = new User("Ani", HASH);
        User userGogo = new User("Gogo", HASH);

        when(repository.getGroup("biggroup")).thenReturn(group);
        when(repository.getUser(IVAN)).thenReturn(userIvan);
        when(repository.getUser(ALEX)).thenReturn(userAlex);
        when(repository.getUser(DANI)).thenReturn(userDani);
        when(repository.getUser("Dave")).thenReturn(userDave);
        when(repository.getUser("Ani")).thenReturn(userAni);
        when(repository.getUser("Gogo")).thenReturn(userGogo);

        splitGroupCommand.execute("600 biggroup party", clientKey);

        assertEquals(100.0, userIvan.getBalances().get(ALEX), 0.01);
        assertEquals(100.0, userIvan.getBalances().get("Ani"), 0.01);
    }

    @Test
    void testExecuteSmallAmount() throws SplitWiseException {
        sessions.put(clientKey, IVAN);
        setupStandardGroupAndUsers();
        splitGroupCommand.execute("0.03 " + GROUP_ROOMMATES + " coffee", clientKey);

        assertEquals(0.01, userIvan.getBalances().get(ALEX), 0.001);
    }

    @Test
    void testExecuteEmptyArguments() {
        sessions.put(clientKey, IVAN);
        assertThrows(
            InvalidCommandArgumentException.class,
            () -> splitGroupCommand.execute("", clientKey)
        );
    }
}