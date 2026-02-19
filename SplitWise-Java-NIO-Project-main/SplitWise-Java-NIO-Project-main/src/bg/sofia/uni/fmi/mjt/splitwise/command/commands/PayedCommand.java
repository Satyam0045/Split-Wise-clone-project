package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

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

public class PayedCommand implements Command {
    private static final int VALID_NUMBER_OF_TOKENS = 2;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public PayedCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws SplitWiseException {

        String currentUser = sessions.get(clientKey);
        if (currentUser == null) {
            throw new NotLoggedInException("Current user is not logged in.");
        }

        String[] tokens = arguments.split("\\s+");
        if (tokens.length != VALID_NUMBER_OF_TOKENS) {
            throw new InvalidCommandArgumentException("Usage: payed <amount> <username>");
        }

        double amount;

        try {
            amount = Double.parseDouble(tokens[0]);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid amount format.");
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive.");
        }

        String payerUsername = tokens[1];

        if (currentUser.equals(payerUsername)) {
            throw new InvalidCommandArgumentException("You cannot approve a payment from yourself.");
        }

        if (!repository.userExists(payerUsername)) {
            throw new UserNotFoundException("User is not found.");
        }

        User me = repository.getUser(currentUser);
        User payer = repository.getUser(payerUsername);

        me.updateBalance(payerUsername, -amount);
        payer.updateBalance(currentUser, amount);
        payer.addNotification(String.format("%s approved your payment %.2f LV.", currentUser, amount));

        repository.saveData();
        repository.logTransaction(currentUser, "Approved payment of " + amount + " from " + payerUsername);

        return String.format("%s payed you %.2f LV. Current status: Owes you %.2f LV",
            payerUsername, amount, me.getBalances().getOrDefault(payerUsername, 0.0));
    }
}