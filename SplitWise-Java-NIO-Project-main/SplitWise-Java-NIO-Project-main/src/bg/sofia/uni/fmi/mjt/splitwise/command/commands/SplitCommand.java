package bg.sofia.uni.fmi.mjt.splitwise.command.commands; // Внимавай с пакета

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidAmountException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class SplitCommand implements Command {
    private static final int VALID_NUMBER_OF_TOKENS = 3;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public SplitCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws SplitWiseException {
        String currentUser = sessions.get(clientKey);
        if (currentUser == null) {
            throw new NotLoggedInException("Current user is not logged in.");
        }

        String[] tokens = arguments.split("\\s+", VALID_NUMBER_OF_TOKENS);
        if (tokens.length < VALID_NUMBER_OF_TOKENS) {
            throw new InvalidCommandArgumentException("Usage: split <amount> <username> <reason>");
        }

        double amount;

        try {
            amount = Double.parseDouble(tokens[0]);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid amount");
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive.");
        }

        String friendName = tokens[1];
        String reason = tokens[2];

        if (currentUser.equals(friendName)) {
            throw new InvalidCommandArgumentException("You cannot split the bill with yourself.");
        }

        User me = repository.getUser(currentUser);
        User friend = repository.getUser(friendName);

        if (friend == null) {
            throw new UserNotFoundException("User is not registered");
        }

        if (!me.getFriends().contains(friendName)) {
            throw new InvalidCommandArgumentException("Current user is not in your friend list. Add them first.");
        }

        double splitAmount = amount / 2.0;
        me.updateBalance(friendName, splitAmount);
        friend.updateBalance(currentUser, -splitAmount);
        friend.addNotification(String.format("User %s split %.2f LV with you for [%s]. You owe %.2f LV.", currentUser, amount, reason, splitAmount));

        repository.saveData();
        repository.logTransaction(currentUser, "Split " + amount + " with " + friendName + " for " + reason);

        return String.format("Splitted %.2f LV. Current status with %s: %.2f LV",
            amount, friendName, me.getBalances().get(friendName));
    }
}