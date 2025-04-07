package tests;

import org.junit.jupiter.api.*;

import banking.User;
import banking.FileSystem;
import banking.Transaction;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemTests {

    private FileSystem fileSystem;
    private Map<String, User> testUsers;
    private Map<String, List<Transaction>> testTransactions;

    @BeforeEach
    void setUp() {
        fileSystem = new FileSystem("testUser.ser", "testTransaction.ser");

        // Setup dummy users
        User user1 = new User("alice", "password", 0);
        User user2 = new User("bob","password1",0);
        testUsers = new HashMap<>();
        testUsers.put("alice", user1);
        testUsers.put("bob", user2);

        // Setup dummy transactions
        Transaction tx1 = new Transaction(100.0, "deposit");
        Transaction tx2 = new Transaction(50.0, "withdraw");
        testTransactions = new HashMap<>();
        testTransactions.put("alice", List.of(tx1));
        testTransactions.put("bob", List.of(tx2));
    }

    @Test
    void testSaveAndLoadUsers() {
        fileSystem.saveUsersToFile(testUsers);
        Map<String, User> loadedUsers = fileSystem.loadUsersFromFile();

        assertEquals(testUsers.size(), loadedUsers.size());
        assertTrue(loadedUsers.containsKey("alice"));
        assertTrue(loadedUsers.containsKey("bob"));
        assertEquals("alice", loadedUsers.get("alice").getUsername());
    }

    @Test
    void testSaveAndLoadTransactions() {
        fileSystem.saveTransactionsToFile(testTransactions);
        Map<String, List<Transaction>> loadedTransactions = fileSystem.loadTransactionsFromFile();

        assertEquals(testTransactions.size(), loadedTransactions.size());
        assertEquals(1, loadedTransactions.get("alice").size());
        assertEquals(100.0, loadedTransactions.get("alice").get(0).getAmount());
    }

    @AfterEach
    void tearDown() {
        new File("testUser.ser").delete();
        new File("testTransaction.ser").delete();
    }
}
