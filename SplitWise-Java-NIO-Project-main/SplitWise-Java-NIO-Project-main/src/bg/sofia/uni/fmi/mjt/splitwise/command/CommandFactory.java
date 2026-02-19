package bg.sofia.uni.fmi.mjt.splitwise.command;

import bg.sofia.uni.fmi.mjt.splitwise.command.commands.AddFriendCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.CreateGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.GetHistoryCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.GetStatusCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.HelpCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.LoginCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.LogoutCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.PayedCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.RegisterCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.SplitCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.SplitGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.command.commands.UnknownCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class CommandFactory {
    private final DataRepository repository;
    private final Map<SelectionKey, String> userSessions;

    public CommandFactory(DataRepository repository, Map<SelectionKey, String> userSessions) {
        this.repository = repository;
        this.userSessions = userSessions;
    }

    public Command createCommand(String commandName) {
        return switch (commandName.toLowerCase()) {
            case "register" -> new RegisterCommand(repository);
            case "login" -> new LoginCommand(repository, userSessions);
            case "logout" -> new LogoutCommand(userSessions);
            case "add-friend" -> new AddFriendCommand(repository, userSessions);
            case "split" -> new SplitCommand(repository, userSessions);
            case "get-status" -> new GetStatusCommand(repository, userSessions);
            case "get-history" -> new GetHistoryCommand(repository, userSessions);
            case "create-group" -> new CreateGroupCommand(repository, userSessions);
            case "split-group" -> new SplitGroupCommand(repository, userSessions);
            case "payed" -> new PayedCommand(repository, userSessions);
            case "help" -> new HelpCommand();
            default -> new UnknownCommand();
        };
    }
}