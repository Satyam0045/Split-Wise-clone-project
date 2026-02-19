package bg.sofia.uni.fmi.mjt.splitwise.model;

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {

    private String username;
    private String passwordHash;
    private Set<String> friends;
    private Map<String, Double> balances;
    private List<String> notifications;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.friends = new HashSet<>();
        this.balances = new HashMap<>();
        this.notifications = new ArrayList<>();
    }

    public void addFriend(String friendUsername) {
        friends.add(friendUsername);
        balances.putIfAbsent(friendUsername, 0.0);
    }

    public void updateBalance(String otherUser, double amount) {
        balances.put(otherUser, balances.getOrDefault(otherUser, 0.0) + amount);
    }

    public void addNotification(String message) {
        notifications.add(message);
    }

    public List<String> getAndClearNotifications() {
        List<String> current = new ArrayList<>(notifications);
        notifications.clear();
        return current;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Map<String, Double> getBalances() {
        return balances;
    }

    public Set<String> getFriends() {
        return friends;
    }
}