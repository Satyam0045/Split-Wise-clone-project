package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateGroupCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private Map<SelectionKey, String> sessions;
    private CreateGroupCommand createGroupCommand;

    @BeforeEach
    void setUp() {
        sessions = new HashMap<>();
        createGroupCommand = new CreateGroupCommand(repository, sessions);
    }

    @Test
    void testExecuteThrowsExceptionWhenNotLoggedIn() {
        String arguments = "groupname user1 user2";

        NotLoggedInException exception = assertThrows(NotLoggedInException.class, () -> createGroupCommand.execute(arguments, clientKey)
        );

        assertEquals("Current user is not logged in.", exception.getMessage());
        verify(repository, never()).addGroup(any(Group.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenOnlyGroupName() {
        String currentUser = "Ivan";
        String arguments = "groupname";
        sessions.put(clientKey, currentUser);

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> createGroupCommand.execute(arguments, clientKey)
        );

        assertTrue(exception.getMessage().contains("Usage"));
        verify(repository, never()).addGroup(any(Group.class));
    }

    @Test
    void testExecuteCreatorAutomaticallyIncluded() throws SplitWiseException {
        String currentUser = "Ivan";
        String arguments = "groupname Alex Dani";
        sessions.put(clientKey, currentUser);

        when(repository.getGroup("groupname")).thenReturn(null);
        when(repository.userExists("Alex")).thenReturn(true);
        when(repository.userExists("Dani")).thenReturn(true);

        createGroupCommand.execute(arguments, clientKey);

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(repository).addGroup(groupCaptor.capture());

        Group group = groupCaptor.getValue();
        assertTrue(group.members().contains("Ivan"),
            "Creator should be automatically included");
    }

    @Test
    void testExecuteMinimumThreeMembers() throws SplitWiseException {
        String currentUser = "Ivan";
        String arguments = "groupname Alex Dani";
        sessions.put(clientKey, currentUser);

        when(repository.getGroup("groupname")).thenReturn(null);
        when(repository.userExists("Alex")).thenReturn(true);
        when(repository.userExists("Dani")).thenReturn(true);

        String result = createGroupCommand.execute(arguments, clientKey);

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(repository).addGroup(groupCaptor.capture());

        Group group = groupCaptor.getValue();
        assertEquals(3, group.members().size());
    }

    @Test
    void testExecuteLargeGroup() throws SplitWiseException {
        String currentUser = "Ivan";
        String arguments = "largegroup Alex Dani dave eve frank";
        sessions.put(clientKey, currentUser);

        when(repository.getGroup("largegroup")).thenReturn(null);
        when(repository.userExists(anyString())).thenReturn(true);

        String result = createGroupCommand.execute(arguments, clientKey);

        assertTrue(result.contains("6 members"));

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(repository).addGroup(groupCaptor.capture());

        Group group = groupCaptor.getValue();
        assertEquals(6, group.members().size());
    }

    @Test
    void testExecuteGroupNameWithSpecialCharacters() throws SplitWiseException {
        String currentUser = "Ivan";
        String arguments = "group_name_123 Alex Dani";
        sessions.put(clientKey, currentUser);

        when(repository.getGroup("group_name_123")).thenReturn(null);
        when(repository.userExists("Alex")).thenReturn(true);
        when(repository.userExists("Dani")).thenReturn(true);

        String result = createGroupCommand.execute(arguments, clientKey);

        assertTrue(result.contains("group_name_123"), "");
    }

    @Test
    void testExecuteEmptyArguments() {
        String currentUser = "Ivan";
        sessions.put(clientKey, currentUser);
        String arguments = "";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> createGroupCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addGroup(any(Group.class));
    }

    @Test
    void testExecuteOnlySpaces() {
        String currentUser = "Ivan";
        sessions.put(clientKey, currentUser);
        String arguments = "   ";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> createGroupCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addGroup(any(Group.class));
    }


    @Test
    void testExecuteFirstNonExistentUserCausesException() {
        String currentUser = "Ivan";
        String arguments = "groupname nonexistent Alex Dani";
        sessions.put(clientKey, currentUser);

        when(repository.getGroup("groupname")).thenReturn(null);
        when(repository.userExists("nonexistent")).thenReturn(false);

        assertThrows(
            UserNotFoundException.class,
            () -> createGroupCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addGroup(any(Group.class));
    }
}
