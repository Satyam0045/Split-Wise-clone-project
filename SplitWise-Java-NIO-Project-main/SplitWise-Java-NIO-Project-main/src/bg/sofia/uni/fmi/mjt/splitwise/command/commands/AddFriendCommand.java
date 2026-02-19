package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import bg.sofia.uni.fmi.mjt.splitwise.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.InvalidCommandArgumentException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.NotLoggedInException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.SplitWiseException;
import bg.sofia.uni.fmi.mjt.splitwise.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.DataRepository;

import java.nio.channels.SelectionKey;
import java.util.Map;


public class AddFriendCommand implements Command {
    private final DataRepository repository;
    private final Map<SelectionKey, String> sessions;

    public AddFriendCommand(DataRepository repository, Map<SelectionKey, String> sessions) {
        this.repository = repository;
        this.sessions = sessions;
    }

    @Override
    public String execute(String arguments, SelectionKey clientKey) throws SplitWiseException {

        String currentUser = sessions.get(clientKey);
        if (currentUser == null) {
            throw new NotLoggedInException("Current user is not logged in.");
        }

        String friendUsername = arguments.strip();
        if (friendUsername.isEmpty()) {
            throw new InvalidCommandArgumentException("Usage: add-friend <username>");
        }

        if (currentUser.equals(friendUsername)) {
            throw new InvalidCommandArgumentException("You cannot add yourself as a friend.");
        }

        if (!repository.userExists(friendUsername)) {
            throw new UserNotFoundException("The user you are trying to add is not registered.");
        }

        User me = repository.getUser(currentUser);
        User friend = repository.getUser(friendUsername);

        if (me.getFriends().contains(friendUsername)) {
            throw new FriendshipAlreadyExistsException("The user is already in your friend list.");
        }

        me.addFriend(friendUsername);
        friend.addFriend(currentUser);

        repository.saveData();
        return "Added " + friendUsername + " to your friend list.";
    }
}
