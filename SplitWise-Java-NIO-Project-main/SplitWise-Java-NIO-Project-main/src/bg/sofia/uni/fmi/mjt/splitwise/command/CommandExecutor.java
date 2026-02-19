package bg.sofia.uni.fmi.mjt.splitwise.command;

import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;
import bg.sofia.uni.fmi.mjt.splitwise.util.ErrorLogger;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private CommandFactory commandFactory;

    public CommandExecutor(DataRepository repository) {
        Map<SelectionKey, String> userSessions = new HashMap<>();
        this.commandFactory = new CommandFactory(repository, userSessions);
    }

    public String execute(String input, SelectionKey key) {
        if (input == null || input.strip().isEmpty()) {
            return "Empty command.";
        }

        String[] tokens = input.strip().split("\\s+", 2);
        String commandName = tokens[0];
        String arguments = tokens.length > 1 ? tokens[1] : "";

        Command command = commandFactory.createCommand(commandName);

        try {
            return command.execute(arguments, key);
        } catch (SplitWiseException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            ErrorLogger.log(e);
            e.printStackTrace();
            return "An unexpected error occurred. Please contact the administrator.";
        }
    }
}