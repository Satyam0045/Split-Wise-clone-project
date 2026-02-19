package bg.sofia.uni.fmi.mjt.splitwise.server.repository;

import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import bg.sofia.uni.fmi.mjt.splitwise.util.ErrorLogger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataRepository {
    private static final String DATA_FILE = "splitwise_data.db";

    private Map<String, User> users;
    private Map<String, Group> groups;

    public DataRepository() {
        if (!loadData()) {
            this.users = new ConcurrentHashMap<>();
            this.groups = new ConcurrentHashMap<>();
        }
    }

    public synchronized void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeObject(groups);
        } catch (IOException e) {
            ErrorLogger.log(e);
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private boolean loadData() {
        if (!Files.exists(Path.of(DATA_FILE))) {
            return false;
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            this.users = (Map<String, User>) inputStream.readObject();
            this.groups = (Map<String, Group>) inputStream.readObject();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            ErrorLogger.log(e);
            System.err.println("Error loading data.");
            return false;
        }
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
        saveData();
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public void addGroup(Group group) {
        groups.put(group.name(), group);
        saveData();
    }

    public Group getGroup(String name) {
        return groups.get(name);
    }

    public Collection<Group> getAllGroups() {
        return groups.values();
    }

    public void logTransaction(String user, String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("transactions_history.txt", true))) {
            writer.write("[" + java.time.LocalDateTime.now() + "] User: " + user + " -> " + message);
            writer.newLine();
        } catch (IOException e) {
            ErrorLogger.log(e);
            System.err.println("Could not write transaction log.");
        }
    }

    public List<String> getTransactionHistory(String username) {
        Path historyPath = Path.of("transactions_history.txt");
        if (!Files.exists(historyPath)) {
            return List.of();
        }

        try (Stream<String> lines = Files.lines(historyPath)) {
            String searchPattern = "User: " + username + " ->";
            return lines.filter(line -> line.contains(searchPattern))
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading transaction history: " + e.getMessage());
            return List.of("Error reading history.");
        }
    }
}