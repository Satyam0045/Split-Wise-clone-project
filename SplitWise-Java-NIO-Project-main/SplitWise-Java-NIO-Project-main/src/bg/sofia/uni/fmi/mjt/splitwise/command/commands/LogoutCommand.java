package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class LogoutCommand implements Command {
    private final Map<SelectionKey, String> sessions;

    public LogoutCommand(Map<SelectionKey, String> sessions) {
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws NotLoggedInException {
        if (!sessions.containsKey(clientKey)) {
            throw new NotLoggedInException("Current user is not logged in.");
        }

        String username = sessions.remove(clientKey);
        return "User " + username + " logged out successfully.";

    }

}