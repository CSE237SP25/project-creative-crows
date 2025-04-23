package tests;

import banking.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class LoanTests {

    private Database db;
    private User user;
    private Administrator admin;

    @BeforeEach
    void setUp() {
        db = new Database();
        user = new User("loanUser", Authenticator.hashPassword("pass123"), 0.0);
        db.createUser(user);
        admin = new Administrator(user,db);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.deleteUser("loanUser");
    }

    @Test
    void testLoanRequestIsRecorded() throws Exception {
        Loan loan = user.requestLoan(200.0, "New Laptop");
        assertNotNull(loan);
        assertFalse(loan.isApproved());
        db.addUserTransaction(user.getUsername(), loan);

        List<Transaction> tx = db.getUserTransaction(user.getUsername());
        assertEquals(1, tx.size());
        assertTrue(tx.get(0) instanceof Loan);
        assertEquals(200.0, tx.get(0).getAmount());
        db.deleteUser(user.getUsername());
    }

    @Test
    void testLoanApprovalIncreasesBalance() {
        Loan loan = user.requestLoan(300.0, "Tuition");
        db.addUserTransaction(user.getUsername(), loan);

        Map<String, List<Transaction>> allTx = db.getAllTransactionMap();
        boolean success = admin.approveLoanById(loan.getTransactionID());

        assertTrue(success);
        assertEquals(300.0, db.getUserData(user.getUsername()).getBalance());
    }

    @Test
    void testLoanCannotBeApprovedTwice() {
        Loan loan = user.requestLoan(150.0, "Emergency");
        db.addUserTransaction(user.getUsername(), loan);

        Map<String, List<Transaction>> allTx = db.getAllTransactionMap();
        boolean firstApproval = admin.approveLoanById(loan.getTransactionID());
        boolean secondApproval = admin.approveLoanById(loan.getTransactionID());

        assertTrue(firstApproval);
        assertFalse(secondApproval);
    }
}
