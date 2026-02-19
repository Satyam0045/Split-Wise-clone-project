package bg.sofia.uni.fmi.mjt.splitwise.command;

import java.nio.channels.SelectionKey;

public interface Command {
    String execute(String arguments, SelectionKey clientKey) throws Exception;
}