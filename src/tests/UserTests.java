package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import banking.Database;
import banking.Loan;
import banking.Transaction;
import banking.User;
import banking.Authenticator;

public class UserTests {
    
    private Database database;

    @BeforeEach
    void setUp() {
        database = new Database();
    }

    @Test
    void testDepositAndWithdraw() throws Exception {
        User testUser = new User("Test", Authenticator.hashPassword("password"), 100.00);
        database.createUser(testUser);

        double depositAmount = 50.00;
        testUser.deposit(depositAmount);
        assertTrue(testUser.getBalance() == 150.00);

        double withdrawAmount = 30.00;
        testUser.withdraw(withdrawAmount);
        assertTrue(testUser.getBalance() == 120.00);

        testUser.withdraw(999.00);
        assertTrue(testUser.getBalance() == 120.00);

        database.deleteUser("Test");
    }

    @Test
    void testDeposit() throws Exception {
        User testUser = new User("Test", Authenticator.hashPassword("password"), 0);
        database.createUser(testUser);

        Transaction depositTransaction = testUser.deposit(200.0);
        assertNotNull(depositTransaction);
        assertEquals(200.0, testUser.getBalance(), 0.01);
        database.deleteUser("Test");
    }

    @Test
    void testWithdraw() throws Exception {
        User testUser = new User("Test", Authenticator.hashPassword("password"), 500);
        database.createUser(testUser);

        Transaction withdrawTransaction = testUser.withdraw(100.0);
        assertNotNull(withdrawTransaction);
        assertEquals(400.0, testUser.getBalance(), 0.01);
        database.deleteUser("Test");
    }

    @Test
    void testWithdrawFailsForInsufficientBalance() throws Exception {
        User testUser = new User("Test", Authenticator.hashPassword("password"), 50);
        database.createUser(testUser);

        Transaction withdrawTransaction = testUser.withdraw(100.0);
        assertNull(withdrawTransaction);
        assertEquals(50.0, testUser.getBalance(), 0.01);
        database.deleteUser("Test");
    }

    @Test
    void testSuccessfulTransfer() throws Exception {
        User sender = new User("Sender", Authenticator.hashPassword("password"), 500);
        User receiver = new User("Receiver", Authenticator.hashPassword("password"), 300);
        database.createUser(sender);
        database.createUser(receiver);

        Transaction transfer = sender.transferTo(receiver, 200.0, "Rent payment");
        assertNotNull(transfer);
        database.addUserTransaction(sender.getUsername(), transfer);

        Transaction receive = receiver.receiveTransfer(200.0, sender.getUsername(), "Rent payment", transfer.getTransactionID());
        database.addUserTransaction(receiver.getUsername(), receive);

        assertEquals(300.0, sender.getBalance(), 0.01);
        assertEquals(500.0, receiver.getBalance(), 0.01);
        database.deleteUser("Sender");
        database.deleteUser("Receiver");
    }

    @Test
    void testTransferFailsForInsufficientFunds() throws Exception {
        User sender = new User("Sender", Authenticator.hashPassword("password"), 100);
        User receiver = new User("Receiver", Authenticator.hashPassword("password"), 300);
        database.createUser(sender);
        database.createUser(receiver);

        Transaction transfer = sender.transferTo(receiver, 200.0, "Large transfer");
        assertNull(transfer);
        assertEquals(100.0, sender.getBalance(), 0.01);
        assertEquals(300.0, receiver.getBalance(), 0.01);
        database.deleteUser("Sender");
        database.deleteUser("Receiver");
    }

    @Test
    void testTransferToSelfFails() throws Exception {
        User sender = new User("SelfUser", Authenticator.hashPassword("password"), 500);
        database.createUser(sender);

        Transaction transfer = sender.transferTo(sender, 100.0, "Trying to send to self");
        assertNull(transfer);
        assertEquals(500.0, sender.getBalance(), 0.01);

        database.deleteUser("SelfUser");
    }

    @Test
    void testTransferWithInvalidAmount() throws Exception {
        User sender = new User("Sender", Authenticator.hashPassword("password"), 500);
        User receiver = new User("Receiver", Authenticator.hashPassword("password"), 500);
        database.createUser(sender);
        database.createUser(receiver);

        Transaction zeroTransfer = sender.transferTo(receiver, 0.0, "Zero transfer");
        Transaction negativeTransfer = sender.transferTo(receiver, -50.0, "Negative transfer");

        assertNull(zeroTransfer);
        assertNull(negativeTransfer);
        assertEquals(500.0, sender.getBalance(), 0.01);
        assertEquals(500.0, receiver.getBalance(), 0.01);
        database.deleteUser("Sender");
        database.deleteUser("Receiver");
    }

    @Test
    void testIssueChargeValidAmount() throws Exception {
        User issuer = new User("UserA", Authenticator.hashPassword("password"), 500);
        User target = new User("UserB", Authenticator.hashPassword("password"), 500);
        database.createUser(issuer);
        database.createUser(target);

        Transaction chargeTransaction = issuer.issueCharge(target, 100.0, "Test charge");
        assertNotNull(chargeTransaction);
        assertEquals(600.0, issuer.getBalance(), 0.01);
        assertEquals(400.0, target.getBalance(), 0.01);
        database.deleteUser("UserA");
        database.deleteUser("UserB");
    }

    @Test
    void testIssueChargeInsufficientBalance() throws Exception {
        User issuer = new User("UserA", Authenticator.hashPassword("password"), 500);
        User target = new User("UserB", Authenticator.hashPassword("password"), 50);
        database.createUser(issuer);
        database.createUser(target);

        Transaction failedCharge = issuer.issueCharge(target, 100.0, "Test charge");
        assertNull(failedCharge);
        assertEquals(500.0, issuer.getBalance(), 0.01);
        assertEquals(50.0, target.getBalance(), 0.01);
        database.deleteUser("UserA");
        database.deleteUser("UserB");
    }

    @Test
    void testIssueChargeInvalidAmount() throws Exception {
        User issuer = new User("UserA", Authenticator.hashPassword("password"), 500);
        User target = new User("UserB", Authenticator.hashPassword("password"), 500);
        database.createUser(issuer);
        database.createUser(target);

        Transaction zeroCharge = issuer.issueCharge(target, 0.0, "Zero charge");
        Transaction negativeCharge = issuer.issueCharge(target, -50.0, "Negative charge");

        assertNull(zeroCharge);
        assertNull(negativeCharge);
        assertEquals(500.0, issuer.getBalance(), 0.01);
        assertEquals(500.0, target.getBalance(), 0.01);
        database.deleteUser("UserA");
        database.deleteUser("UserB");
    }

    @Test
    void testIssuerStatementIncludesChargeTransaction() throws Exception {
        User issuer = new User("Issuer", Authenticator.hashPassword("password"), 500);
        User target = new User("Target", Authenticator.hashPassword("password"), 500);
        database.createUser(issuer);
        database.createUser(target);

        Transaction deposit = issuer.deposit(100.0);
        Transaction charge = issuer.issueCharge(target, 50.0, "Test Service");
        database.addUserTransaction(issuer.getUsername(), deposit);
        database.addUserTransaction(issuer.getUsername(), charge);

        List<Transaction> issuerTransactions = database.getUserTransaction(issuer.getUsername());
        assertEquals(2, issuerTransactions.size());
        assertEquals("Deposit", issuerTransactions.get(0).getDescription());
        assertTrue(issuerTransactions.get(1).getDescription().contains("Test Service"));
        database.deleteUser("Issuer");
        database.deleteUser("Target");
    }

    @Test
    void testTargetStatementIncludesChargeReceived() throws Exception {
        User issuer = new User("Issuer", Authenticator.hashPassword("password"), 500);
        User target = new User("Target", Authenticator.hashPassword("password"), 500);
        database.createUser(issuer);
        database.createUser(target);

        Transaction chargeIssued = issuer.issueCharge(target, 50.0, "Test Service");
        Transaction chargeRecord = target.issueChargeRecord(50.0, issuer.getUsername(), "Test Service", chargeIssued.getTransactionID());
        database.addUserTransaction(target.getUsername(), chargeRecord);

        List<Transaction> targetTransactions = database.getUserTransaction(target.getUsername());
        assertEquals(1, targetTransactions.size());
        assertTrue(targetTransactions.get(0).getDescription().contains("Issuer"));
        database.deleteUser("Issuer");
        database.deleteUser("Target");
    }

    @Test
    void testSuccessfulLoanRepayment() throws Exception {
        User testUser = new User("LoanPayer", Authenticator.hashPassword("password"), 500.0);
        database.createUser(testUser);

        Loan loan = new Loan(testUser.getAccountNumber(),200.0, "Loan request - laptop");
        loan.approve(database);
        Transaction loanTx = loan;
        database.addUserTransaction(testUser.getUsername(), loanTx);

        Transaction repayment = testUser.repayLoan(loan, 150.0);
        assertNotNull(repayment);
        assertEquals(550.0, testUser.getBalance(), 0.01);
        assertEquals(150.0, loan.getAmountPaid(), 0.01);
        assertFalse(loan.isPaidOff());

        Transaction finalRepayment = testUser.repayLoan(loan, 50.0);
        assertNotNull(finalRepayment);
        assertTrue(loan.isPaidOff());
        assertEquals(500.0, testUser.getBalance(), 0.01);
        database.deleteUser("LoanPayer");
    }

    @Test
    void testRepaymentFailsForUnapprovedLoan() throws Exception {
        User testUser = new User("UnapprovedLoanUser", Authenticator.hashPassword("password"), 500.0);
        database.createUser(testUser);

        Loan loan = new Loan(testUser.getAccountNumber(),200.0, "Loan request - book");
        Transaction repayment = testUser.repayLoan(loan, 100.0);

        assertNull(repayment);
        assertEquals(500.0, testUser.getBalance(), 0.01);
        database.deleteUser("UnapprovedLoanUser");
    }

    @Test
    void testOverRepaymentIsCapped() throws Exception {
        User testUser = new User("Overpayer", Authenticator.hashPassword("password"), 500.0);
        database.createUser(testUser);

        Loan loan = new Loan(testUser.getAccountNumber(),100.0, "Loan request - gift card");
        loan.approve(database);
        database.addUserTransaction(testUser.getUsername(), loan);

        Transaction partialRepayment = testUser.repayLoan(loan, 80.0);
        assertNotNull(partialRepayment);
        assertEquals(520.0, testUser.getBalance(), 0.01);
        assertEquals(80.0, loan.getAmountPaid(), 0.01);
        assertFalse(loan.isPaidOff());

        Transaction cappedRepayment = testUser.repayLoan(loan, 50.0);
        assertNotNull(cappedRepayment);
        assertEquals(500.0, testUser.getBalance(), 0.01);
        assertEquals(100.0, loan.getAmountPaid(), 0.01);
        assertTrue(loan.isPaidOff());
        database.deleteUser("Overpayer");
    }

}