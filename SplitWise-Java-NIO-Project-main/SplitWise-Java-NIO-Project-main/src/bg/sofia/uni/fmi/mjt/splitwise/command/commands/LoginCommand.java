package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.AlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Map;

public class LoginCommand implements Command {
    private static final int VALID_NUMBER_OF_TOKENS = 2;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public LoginCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws SplitWiseException {

        if (sessions.containsKey(clientKey)) {
            throw new AlreadyLoggedInException("You are already logged in");
        }

        String[] tokens = arguments.split("\\s+");

        if (tokens.length != VALID_NUMBER_OF_TOKENS) {
            throw new InvalidCommandArgumentException("Usage: login <username> <password>");
        }

        String username = tokens[0];
        String passwordHash = String.valueOf(tokens[1].hashCode());

        User user = repository.getUser(username);
        if (user == null || !user.getPasswordHash().equals(passwordHash)) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        sessions.put(clientKey, username);

        StringBuilder response = new StringBuilder("Successful login!\n");
        List<String> notifications = user.getAndClearNotifications();

        if (notifications.isEmpty()) {
            response.append("No notifications to show.");
        } else {
            response.append("*** Notifications ***\n");
            notifications.forEach(n -> response.append(n).append("\n"));
            repository.saveData();
        }

        return response.toString();
    }
}