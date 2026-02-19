package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class GetStatusCommand implements Command {
    private static final double MIN_SIGNIFICANT_AMOUNT = 0.01;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public GetStatusCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws NotLoggedInException {
        String currentUser = sessions.get(clientKey);
        if (currentUser == null) {
            throw new NotLoggedInException("Current user is not logged in");
        }

        User me = repository.getUser(currentUser);
        Map<String, Double> balances = me.getBalances();
        StringBuilder sb = new StringBuilder();

        sb.append("Friends:\n");
        boolean hasFriends = false;
        for (String friendName : me.getFriends()) {
            double amount = balances.getOrDefault(friendName, 0.0);

            if (Math.abs(amount) >= MIN_SIGNIFICANT_AMOUNT) {
                hasFriends = true;
                sb.append("* ").append(friendName).append(": ");

                if (amount > 0) {
                    sb.append(String.format("Owes you %.2f LV", amount));
                } else {
                    sb.append(String.format("You owe %.2f LV", Math.abs(amount)));
                }

                sb.append("\n");
            }
        }

        if (!hasFriends) {
            sb.append("No debts with friends.\n");
        }

        sb.append("\nGroups:\n");
        boolean hasGroups = false;

        for (Group group : repository.getAllGroups()) {
            if (group.members().contains(currentUser)) {
                hasGroups = true;
                sb.append("* ").append(group.name()).append("\n");

                for (String memberName : group.members()) {
                    if (memberName.equals(currentUser)) continue;

                    double amount = balances.getOrDefault(memberName, 0.0);

                    if (Math.abs(amount) >= MIN_SIGNIFICANT_AMOUNT) {
                        sb.append("  - ").append(memberName).append(": ");
                        if (amount > 0) {
                            sb.append(String.format("Owes you %.2f LV", amount));
                        } else {
                            sb.append(String.format("You owe %.2f LV", Math.abs(amount)));
                        }
                        sb.append("\n");
                    }
                }
            }
        }

        if (!hasGroups) {
            sb.append("No groups.\n");
        }

        return sb.toString().strip();
    }
}