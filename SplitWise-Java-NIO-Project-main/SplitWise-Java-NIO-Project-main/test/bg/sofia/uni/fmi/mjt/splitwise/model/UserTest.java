package bg.sofia.uni.fmi.mjt.splitwise.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {
    private User user;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD_HASH = "hashedPassword123";

    @BeforeEach
    void setUp() {
        user = new User(USERNAME, PASSWORD_HASH);
    }

    @Test
    void testConstructorCreatesUserWithCorrectProperties() {
        assertEquals(USERNAME, user.getUsername(), "Username should match");
        assertEquals(PASSWORD_HASH, user.getPasswordHash(), "Password hash should match");
        assertNotNull(user.getFriends(), "Friends set should not be null");
        assertNotNull(user.getBalances(), "Balances map should not be null");
        assertTrue(user.getFriends().isEmpty(), "Friends set should be empty initially");
        assertTrue(user.getBalances().isEmpty(), "Balances map should be empty initially");
    }

    @Test
    void testAddFriendAddsNewFriend() {
        String friendName = "Ivan";

        user.addFriend(friendName);

        assertTrue(user.getFriends().contains(friendName), "Friend should be added to friends set");
        assertEquals(0.0, user.getBalances().get(friendName), "Initial balance should be 0.0");
    }

    @Test
    void testAddFriendDoesNotDuplicateFriend() {
        String friendName = "Ivan";

        user.addFriend(friendName);
        user.addFriend(friendName);

        Set<String> friends = user.getFriends();
        assertEquals(1, friends.size(), "Friend should not be duplicated");
    }

    @Test
    void testUpdateBalanceIncreasesBalance() {
        String friendName = "Ivan";
        user.addFriend(friendName);

        user.updateBalance(friendName, 50.0);

        assertEquals(50.0, user.getBalances().get(friendName), 0.01,
            "Balance should be increased by 50.0");
    }

    @Test
    void testUpdateBalanceDecreasesBalance() {
        String friendName = "Ivan";
        user.addFriend(friendName);
        user.updateBalance(friendName, 100.0);
        user.updateBalance(friendName, -30.0);

        assertEquals(70.0, user.getBalances().get(friendName), 0.01,
            "Balance should be decreased by 30.0");
    }

    @Test
    void testUpdateBalanceHandlesNegativeBalance() {
        String friendName = "Ivan";
        user.addFriend(friendName);

        user.updateBalance(friendName, -25.0);

        assertEquals(-25.0, user.getBalances().get(friendName), 0.01,
            "Balance should handle negative values");
    }

    @Test
    void testUpdateBalanceWithNonExistentUserCreatesBalance() {
        String friendName = "Alex";

        user.updateBalance(friendName, 75.0);

        assertEquals(75.0, user.getBalances().get(friendName), 0.01,
            "Balance should be created for non-existent user");
    }

    @Test
    void testUpdateBalanceMultipleUpdatesAccumulatesCorrectly() {
        String friendName = "Ivan";
        user.addFriend(friendName);

        user.updateBalance(friendName, 10.0);
        user.updateBalance(friendName, 20.0);
        user.updateBalance(friendName, -5.0);

        assertEquals(25.0, user.getBalances().get(friendName), 0.01,
            "Multiple updates should accumulate correctly");
    }

    @Test
    void testAddNotificationAddsNotificationToList() {
        String notification = "You owe Ivan 50 LV";

        user.addNotification(notification);

        List<String> notifications = user.getAndClearNotifications();
        assertEquals(1, notifications.size(), "Should have one notification");
        assertEquals(notification, notifications.get(0), "Notification content should match");
    }

    @Test
    void testAddNotificationAddsMultipleNotifications() {
        user.addNotification("Notification 1");
        user.addNotification("Notification 2");
        user.addNotification("Notification 3");

        List<String> notifications = user.getAndClearNotifications();

        assertEquals(3, notifications.size(), "Should have three notifications");
        assertEquals("Notification 1", notifications.get(0));
        assertEquals("Notification 2", notifications.get(1));
        assertEquals("Notification 3", notifications.get(2));
    }

    @Test
    void testGetAndClearNotificationsClearsNotifications() {
        user.addNotification("Test notification");

        user.getAndClearNotifications();
        List<String> secondFetch = user.getAndClearNotifications();

        assertTrue(secondFetch.isEmpty(), "Notifications should be cleared after first fetch");
    }

    @Test
    void testGetAndClearNotificationsReturnsEmptyListWhenNoNotifications() {
        List<String> notifications = user.getAndClearNotifications();

        assertNotNull(notifications, "Should return non-null list");
        assertTrue(notifications.isEmpty(), "Should return empty list when no notifications");
    }

    @Test
    void testGetBalancesReturnsCorrectMap() {
        user.addFriend("Ivan");
        user.addFriend("Alex");
        user.updateBalance("Ivan", 50.0);
        user.updateBalance("Alex", -25.0);

        Map<String, Double> balances = user.getBalances();

        assertEquals(2, balances.size(), "Should have balances for 2 friends");
        assertEquals(50.0, balances.get("Ivan"), 0.01);
        assertEquals(-25.0, balances.get("Alex"), 0.01);
    }

    @Test
    void testGetFriendsReturnsCorrectSet() {
        user.addFriend("Ivan");
        user.addFriend("Alex");
        user.addFriend("Dani");

        Set<String> friends = user.getFriends();

        assertEquals(3, friends.size(), "Should have 3 friends");
        assertTrue(friends.contains("Ivan"));
        assertTrue(friends.contains("Alex"));
        assertTrue(friends.contains("Dani"));
    }

    @Test
    void testComplexScenarioMultipleOperations() {

        user.addFriend("Ivan");
        user.addFriend("Alex");

        user.updateBalance("Ivan", 100.0);
        user.updateBalance("Alex", -50.0);
        user.updateBalance("Ivan", -25.0);

        user.addNotification("Ivan paid 25 LV");
        user.addNotification("You owe Alex 50 LV");

        assertEquals(75.0, user.getBalances().get("Ivan"), 0.01);
        assertEquals(-50.0, user.getBalances().get("Alex"), 0.01);

        List<String> notifications = user.getAndClearNotifications();
        assertEquals(2, notifications.size());
        assertTrue(user.getAndClearNotifications().isEmpty());
    }
}
