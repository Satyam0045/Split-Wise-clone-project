package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;

import java.nio.channels.SelectionKey;

public class UnknownCommand implements Command {
    @Override
    public String execute(String arguments, SelectionKey clientKey) {
        return "Unknown command. Type 'help' to see available commands.";
    }
}