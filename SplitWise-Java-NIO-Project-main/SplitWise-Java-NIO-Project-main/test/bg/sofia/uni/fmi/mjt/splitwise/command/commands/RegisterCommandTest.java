package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCommandTest {

    @Mock
    private DataRepository repository;

    @Mock
    private SelectionKey clientKey;

    private RegisterCommand registerCommand;

    @BeforeEach
    void setUp() {
        registerCommand = new RegisterCommand(repository);
    }

    @Test
    void testExecuteSuccessfulRegistration() throws SplitWiseException {
        String username = "john_doe";
        String password = "password123";
        String arguments = username + " " + password;

        when(repository.userExists(username)).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertEquals("Registration successful for john_doe", result);
        verify(repository, times(1)).userExists(username);
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenInvalidNumberOfArguments() {
        String arguments = "onlyusername";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        assertEquals("Usage: register <username> <password>", exception.getMessage());
        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenTooManyArguments() {
        String arguments = "user pass extra";

        assertThrows(InvalidCommandArgumentException.class, () -> registerCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenUsernameTooShort() {
        String arguments = "ab password123";

        InvalidCommandArgumentException exception = assertThrows(InvalidCommandArgumentException.class,  () -> registerCommand.execute(arguments, clientKey)
        );

        assertEquals("Username must be at least 3 characters.", exception.getMessage());
        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenPasswordTooShort() {
        String arguments = "username short";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        assertEquals("Password must be at least 6 characters.", exception.getMessage());
        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenUsernameContainsInvalidCharacters() {
        String arguments = "user@name password123";

        InvalidCommandArgumentException exception = assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        assertEquals("Username can only contain letters, numbers, and underscores.",
            exception.getMessage());
        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenUsernameContainsSpaces() {
        String arguments = "user name password123";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenUsernameContainsSpecialChars() {
        String[] invalidUsernames = {"user-name", "user.name", "user#name", "user!name"};

        for (String username : invalidUsernames) {
            String arguments = username + " password123";

            assertThrows(
                InvalidCommandArgumentException.class,
                () -> registerCommand.execute(arguments, clientKey),
                "Should reject username: " + username
            );
        }

        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteAcceptsValidUsernameWithUnderscores() throws SplitWiseException {
        String arguments = "user_name_123 password123";

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteThrowsExceptionWhenUserAlreadyExists() {
        String arguments = "existinguser password123";

        when(repository.userExists("existinguser")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        assertEquals("User already exists.", exception.getMessage());
        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteMinimumValidUsernameLength() throws SplitWiseException {
        String arguments = "abc password123";

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteMinimumValidPasswordLength() throws SplitWiseException {
        String arguments = "username pass12";

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteLongUsernameAndPassword() throws SplitWiseException {
        String longUsername = "a".repeat(50);
        String longPassword = "p".repeat(50);
        String arguments = longUsername + " " + longPassword;

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteEmptyArguments() {
        String arguments = "";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteOnlySpaces() {
        String arguments = "   ";

        assertThrows(
            InvalidCommandArgumentException.class,
            () -> registerCommand.execute(arguments, clientKey)
        );

        verify(repository, never()).addUser(any(User.class));
    }

    @Test
    void testExecuteNumericUsername() throws SplitWiseException {
        String arguments = "123456 password123";

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }

    @Test
    void testExecuteAlphanumericMixedUsername() throws SplitWiseException {
        String arguments = "User123_ABC password123";

        when(repository.userExists(anyString())).thenReturn(false);

        String result = registerCommand.execute(arguments, clientKey);

        assertTrue(result.contains("successful"));
        verify(repository, times(1)).addUser(any(User.class));
    }
}
