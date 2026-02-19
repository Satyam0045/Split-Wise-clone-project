package bg.sofia.uni.fmi.mjt.splitwise.server.repository;

import bg.sofia.uni.fmi.mjt.splitwise.model.Group;
import bg.sofia.uni.fmi.mjt.splitwise.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryTest {

    private static final String TEST_DATA_FILE = "splitwise_data.db";
    private static final String TEST_TRANSACTION_FILE = "transactions_history.txt";

    private DataRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        cleanUpTestFiles();
        repository = new DataRepository();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanUpTestFiles();
    }

    private void cleanUpTestFiles() throws IOException {
        Files.deleteIfExists(Path.of(TEST_DATA_FILE));
        Files.deleteIfExists(Path.of(TEST_TRANSACTION_FILE));
    }

    @Test
    void testConstructorInitializesEmptyRepository() {
        assertNotNull(repository.getAllGroups());
        assertTrue(repository.getAllGroups().isEmpty());
    }

    @Test
    void testAddUserAddsNewUser() {
        User user = new User("Ivan", "hash123");

        repository.addUser(user);
        assertTrue(repository.userExists("Ivan"));
        assertEquals(user, repository.getUser("Ivan"));
    }

    @Test
    void testGetUserReturnsNullForNonExistentUser() {
        User user = repository.getUser("nonexistent");
        assertNull(user);
    }

    @Test
    void testUserExists_ReturnsTrueForExistingUser() {
        User user = new User("Dani", "hash789");
        repository.addUser(user);
        assertTrue(repository.userExists("Dani"));
    }

    @Test
    void testUserExists_ReturnsFalseForNonExistentUser() {
        assertFalse(repository.userExists("nonexistent"));
    }

    @Test
    void testAddUserOverwritesExistingUser() {
        User user1 = new User("dave", "oldhash");
        User user2 = new User("dave", "newhash");

        repository.addUser(user1);
        repository.addUser(user2);

        User retrieved = repository.getUser("dave");
        assertEquals("newhash", retrieved.getPasswordHash());
    }

    @Test
    void testAddGroupAddsNewGroup() {
        Group group = new Group("roommates", Set.of("Ivan", "Alex", "Dani"));

        repository.addGroup(group);

        assertNotNull(repository.getGroup("roommates"));
        assertEquals(group, repository.getGroup("roommates"));
    }

    @Test
    void testGetGroupReturnsNullForNonExistentGroup() {
        Group group = repository.getGroup("nonexistent");
        assertNull(group);
    }

    @Test
    void testGetAllGroupsReturnsEmptyCollectionInitially() {
        Collection<Group> groups = repository.getAllGroups();
        assertNotNull(groups);
        assertTrue(groups.isEmpty());
    }

    @Test
    void testGetAllGroupsReturnsAllGroups() {
        Group group1 = new Group("group1", Set.of("a", "b", "c"));
        Group group2 = new Group("group2", Set.of("d", "e", "f"));
        Group group3 = new Group("group3", Set.of("g", "h", "i"));

        repository.addGroup(group1);
        repository.addGroup(group2);
        repository.addGroup(group3);

        Collection<Group> groups = repository.getAllGroups();
        assertEquals(3, groups.size());
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
        assertTrue(groups.contains(group3));
    }

    @Test
    void testLogTransactionCreatesFile() throws IOException {
        repository.logTransaction("Ivan", "Split 100 with Alex");
        assertTrue(Files.exists(Path.of(TEST_TRANSACTION_FILE)));
    }

    @Test
    void testLogTransactionWritesCorrectFormat() throws IOException {
        String username = "Ivan";
        String message = "Split 100 with Alex for dinner";

        repository.logTransaction(username, message);

        List<String> lines = Files.readAllLines(Path.of(TEST_TRANSACTION_FILE));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("User: Ivan"));
        assertTrue(lines.get(0).contains(message));
    }

    @Test
    void testGetTransactionHistoryReturnsEmptyForNoHistory() {
        List<String> history = repository.getTransactionHistory("Ivan");
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetTransactionHistoryDoesNotReturnOtherUsersTransactions() {
        repository.logTransaction("Ivan", "Ivan transaction");
        repository.logTransaction("Alex", "Alex transaction");

        List<String> AlexHistory = repository.getTransactionHistory("Alex");

        assertEquals(1, AlexHistory.size());
        assertFalse(AlexHistory.get(0).contains("Ivan"));
        assertTrue(AlexHistory.get(0).contains("Alex"));
    }

    @Test
    void testPersistenceMultipleUsersAndGroups() throws IOException {
        repository.addUser(new User("user1", "hash1"));
        repository.addUser(new User("user2", "hash2"));
        repository.addUser(new User("user3", "hash3"));

        repository.addGroup(new Group("group1", Set.of("user1", "user2")));
        repository.addGroup(new Group("group2", Set.of("user2", "user3")));

        DataRepository newRepo = new DataRepository();

        assertTrue(newRepo.userExists("user1"));
        assertTrue(newRepo.userExists("user2"));
        assertTrue(newRepo.userExists("user3"));
        assertNotNull(newRepo.getGroup("group1"));
        assertNotNull(newRepo.getGroup("group2"));
    }

    @Test
    void testAddGroupOverwritesExistingGroup() {
        Group group1 = new Group("test", Set.of("a", "b"));
        Group group2 = new Group("test", Set.of("c", "d", "e"));

        repository.addGroup(group1);
        repository.addGroup(group2);

        Group retrieved = repository.getGroup("test");
        assertEquals(3, retrieved.members().size());
        assertTrue(retrieved.members().contains("c"));
    }

    @Test
    void testConcurrentAccessMultipleUsers() {
        User user1 = new User("user1", "hash1");
        User user2 = new User("user2", "hash2");
        User user3 = new User("user3", "hash3");

        repository.addUser(user1);
        repository.addUser(user2);
        repository.addUser(user3);

        assertTrue(repository.userExists("user1"));
        assertTrue(repository.userExists("user2"));
        assertTrue(repository.userExists("user3"));
    }

    @Test
    void testGetUserReturnsCorrectUserAfterMultipleAdds() {
        repository.addUser(new User("user1", "hash1"));
        repository.addUser(new User("user2", "hash2"));
        repository.addUser(new User("user3", "hash3"));

        User retrieved = repository.getUser("user2");

        assertNotNull(retrieved);
        assertEquals("user2", retrieved.getUsername());
        assertEquals("hash2", retrieved.getPasswordHash());
    }

    @Test
    void testGetTransactionHistoryOrderPreserved() {
        repository.logTransaction("Ivan", "First");
        repository.logTransaction("Ivan", "Second");
        repository.logTransaction("Ivan", "Third");

        List<String> history = repository.getTransactionHistory("Ivan");

        assertTrue(history.get(0).contains("First"));
        assertTrue(history.get(1).contains("Second"));
        assertTrue(history.get(2).contains("Third"));
    }

}
