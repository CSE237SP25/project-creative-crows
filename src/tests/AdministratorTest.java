package tests;

import banking.Administrator;
import banking.Database;
import banking.Loan;
import banking.Transaction;
import banking.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AdministratorTest {

    private Database db;
    private User testUser;
    private Administrator admin;
    private String testUsername = "adminTestUser";

    @BeforeEach
    public void setUp() throws Exception {
    	db = new Database();
        testUser = new User(testUsername, "adminpass", 1000.0);
        admin = new Administrator(testUser,db);
    }

    @Test
    void testRecallTransactionsFromAdmin() throws Exception {
    	db.createUser(testUser);
        Transaction tx = new Transaction(45.0,"Membership Fee");
        db.addUserTransaction(testUsername, tx);

        HashMap<User, Transaction> toRecall = db.recallTransaction(tx.getTransactionID());

        assertEquals(1, toRecall.size());
        assertTrue(toRecall.containsKey(testUser));
        assertEquals(tx.getTransactionID(), toRecall.get(testUser).getTransactionID());

        admin.recallTransactions(toRecall);

        List<Transaction> txs = db.getUserTransaction(testUsername);
        assertTrue(txs.stream().noneMatch(t -> t.getTransactionID().equals(tx.getTransactionID())));
        db.deleteUser(testUsername);
    }

    @Test
    void testPrintAllTransactions() throws Exception {
    	db.createUser(testUser);
        db.addUserTransaction(testUsername, new Transaction(100.0, "Deposit"));
        db.addUserTransaction(testUsername, new Transaction(20.0, "Withdrawal"));
        db.addUserTransaction(testUsername, new Transaction(30.0, "Withdrawal"));

        List<Transaction> all = db.getAllTransactions();

        // Just verify nothing crashes and expected number of lines exist
        assertEquals(3, all.size());
        admin.printAllTransactions(all);
        db.deleteUser(testUsername);
    }

    @Test
    void testShowAllLoansWithStatus() throws Exception {
        db.createUser(testUser);
        Loan loan1 = testUser.requestLoan(150.0, "Emergency");
        Loan loan2 = testUser.requestLoan(200.0, "Vacation");
        db.addUserTransaction(testUsername, loan1);
        db.addUserTransaction(testUsername, loan2);

        Map<String, List<Transaction>> allTx = db.getAllTransactionMap();

        assertDoesNotThrow(() -> admin.showAllLoansWithStatus());
        db.deleteUser(testUsername);
    }

    @Test
    void testApproveLoanByIdSuccessfulApproval() throws Exception {
        db.createUser(testUser);
        Loan loan = testUser.requestLoan(200.0, "Bike");
        db.addUserTransaction(testUsername, loan);

        Map<String, List<Transaction>> allTx = db.getAllTransactionMap();
        boolean result = admin.approveLoanById(loan.getTransactionID());

        assertTrue(result);
        assertTrue(loan.isApproved());
        assertEquals(1200.0, db.getUserData(testUsername).getBalance());
        db.deleteUser(testUsername);
    }

    @Test
    void testApproveLoanByIdAlreadyApproved() throws Exception {
        db.createUser(testUser);
        Loan loan = testUser.requestLoan(100.0, "Laptop");
        db.addUserTransaction(testUsername, loan);
        loan.approve(db);

        Map<String, List<Transaction>> allTx = db.getAllTransactionMap();
        boolean result = admin.approveLoanById(loan.getTransactionID());

        assertFalse(result);
        db.deleteUser(testUsername);
    }

    @Test
    void testAdminInheritance() {
        assertEquals(testUsername, admin.getUsername());
        assertEquals("adminpass", admin.getHashedPassword());
        assertEquals(1000.0, admin.getBalance());
    }
}
