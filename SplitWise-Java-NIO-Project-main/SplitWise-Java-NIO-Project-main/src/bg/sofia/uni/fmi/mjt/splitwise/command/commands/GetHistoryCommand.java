package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Map;

public class GetHistoryCommand implements Command {
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public GetHistoryCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws NotLoggedInException {

        String currentUser = sessions.get(clientKey);
        if (currentUser == null) {
            throw new NotLoggedInException("Current user is not logged in");
        }

        List<String> history = repository.getTransactionHistory(currentUser);

        if (history.isEmpty()) {
            return "You have no transaction history yet.";
        }

        StringBuilder sb = new StringBuilder("*** Payment History ***\n");
        for (String record : history) {
            sb.append(record).append(System.lineSeparator());
        }

        return sb.toString().strip();
    }
}