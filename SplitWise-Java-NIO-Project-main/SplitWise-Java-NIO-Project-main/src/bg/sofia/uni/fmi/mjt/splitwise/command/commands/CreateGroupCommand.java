package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Currency;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateGroupCommand implements Command {

    private static final int MIN_NUMBER_OF_TOKENS = 3;
    private static final int MIN_MEMBERS = 3;
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public CreateGroupCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
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

        if (tokens.length < MIN_NUMBER_OF_TOKENS) {
            throw new InvalidCommandArgumentException(
                "Usage: create-group <group_name> <user1> <user2> ... (min 3 members total)");
        }

        String groupName = tokens[0];
        if (repository.getGroup(groupName) != null) {
            throw new GroupAlreadyExistsException("Group already exists.");
        }

        Set<String> members = new HashSet<>();
        members.add(currentUser);

        for (int i = 1; i < tokens.length; i++) {
            String username = tokens[i];

            if (username.equals(currentUser)) {
                continue;
            }

            if (!repository.userExists(username)) {
                throw new UserNotFoundException("User is not found.");
            }

            if (members.contains(username)) {
                throw new InvalidCommandArgumentException("User is added twice.");
            }

            members.add(username);
        }

        if (members.size() < MIN_MEMBERS) {
            throw new InvalidCommandArgumentException("Group must have at least 3 distinct members (including you).");
        }

        Group newGroup = new Group(groupName, members);
        repository.addGroup(newGroup);
        return "Group " + groupName + " created successfully with " + members.size() + " members.";
    }
}