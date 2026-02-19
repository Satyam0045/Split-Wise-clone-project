package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;

import java.nio.channels.SelectionKey;

public class HelpCommand implements Command {
    @Override
    public String execute(String arguments, SelectionKey clientKey) {
        return """
            Available commands:
            - register <username> <password>
            - login <username> <password>
            - logout
            - add-friend <username>
            - split <amount> <username> <reason>
            - payed <amount> <username>
            - get-status
            - get-history
            - create-group <group_name> <user1> <user2> ...
            - split-group <amount> <group_name> <reason>
            - help
            """;
    }
}