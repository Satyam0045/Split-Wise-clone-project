package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.GroupNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidAmountException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class SplitGroupCommand implements Command {
    private static final int VALID_NUMBER_OF_TOKENS = 3;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public SplitGroupCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
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
            throw new InvalidCommandArgumentException("Usage: split-group <amount> <group_name> <reason>");
        }

        double amount;
        try {
            amount = Double.parseDouble(tokens[0]);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid amount.", e);
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive.");
        }

        String groupName = tokens[1];
        String reason = tokens[2];

        Group group = repository.getGroup(groupName);

        if (group == null) {
            throw new GroupNotFoundException("Group is not found");
        }

        if (!group.members().contains(currentUser)) {
            throw new InvalidCommandArgumentException("Current user is NOT a member of the group.");
        }

        int memberCount = group.members().size();
        double splitAmount = amount / memberCount;
        User me = repository.getUser(currentUser);

        for (String memberName : group.members()) {
            if (memberName.equals(currentUser)) {
                continue;
            }

            User member = repository.getUser(memberName);
            me.updateBalance(memberName, splitAmount);
            member.updateBalance(currentUser, -splitAmount);

            member.addNotification(String.format(
                "Group %s: %s paid %.2f LV [%s]. You owe %.2f LV.",
                groupName, currentUser, amount, reason, splitAmount
            ));
        }

        repository.saveData();
        repository.logTransaction(currentUser, "Split group " + groupName + ": " + amount + " for " + reason);

        return String.format("Split %.2f LV in group %s. Each member owes %.2f LV.",
            amount, groupName, splitAmount);
    }
}