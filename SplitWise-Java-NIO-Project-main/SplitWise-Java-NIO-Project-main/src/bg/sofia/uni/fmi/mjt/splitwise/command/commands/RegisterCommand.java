package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;

public class RegisterCommand implements Command {
    private static final int VALID_NUMBER_OF_TOKENS = 2;
    private static final int MIN_USERNAME_LEN = 3;
    private static final int MIN_PASSWORD_LEN = 6;
    private final DataRepository repository;

    public RegisterCommand(DataRepository repository) {
        this.repository = repository;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws SplitWiseException {

        String[] tokens = arguments.split("\\s+");
        if (tokens.length != VALID_NUMBER_OF_TOKENS) {
            throw new InvalidCommandArgumentException("Usage: register <username> <password>");
        }

        String username = tokens[0];
        String password = tokens[1];

        if (username.length() < MIN_USERNAME_LEN) {
            throw new InvalidCommandArgumentException("Username must be at least 3 characters.");
        }
        if (password.length() < MIN_PASSWORD_LEN) {
            throw new InvalidCommandArgumentException("Password must be at least 6 characters.");
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            throw new InvalidCommandArgumentException("Username can only contain letters, numbers, and underscores.");
        }

        if (repository.userExists(username)) {
            throw new UserAlreadyExistsException("User already exists.");
        }

        String passwordHash = String.valueOf(password.hashCode());
        User newUser = new User(username, passwordHash);
        repository.addUser(newUser);
        return "Registration successful for " + username;
    }
}