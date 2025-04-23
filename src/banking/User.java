package banking;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {

    private String username;
    private String hashedPassword;
    private double balance;
    private String recoverySecret;
    private String accountNumber;
    private int authLevel;
    private double debt;
    private boolean frozen = false;
    
    public User(String username, String hashedPassword, double balance) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.balance = balance;
        this.accountNumber = UUID.randomUUID().toString();
        this.authLevel = 0;
        this.debt = 0;
    }
    
    public String getAccountNumber() {
		return accountNumber;
    }

    public void setAuthLevel(int authLevel) {
        this.authLevel = authLevel;
    }

    public boolean isAuthorizedFor(int requiredClearance) {
        return this.authLevel >= requiredClearance;
    }
    
    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getUsername() {
        return username;
    }
    
    public double getBalance() {
        return balance;
    }

    public String getSecret() {
        if (recoverySecret != null) {
            return recoverySecret;
        }
        return null;
    }

    public void resetPassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
    }
    
    public void changeUsername(String newUsername) {
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            this.username = newUsername;
        }
    }

    public void setSecret(String secret) {
        this.recoverySecret = secret;
    }
    //to check if two users are the same
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Double.compare(user.balance, balance) == 0 &&
               username.equals(user.username) &&
               hashedPassword.equals(user.hashedPassword);
    }

    //override user hashcode
    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword, balance);
    }
    
    public boolean isFrozen() {
        return frozen;
    }

    public void freezeAccount() {
        this.frozen = true;
    }

    public void unfreezeAccount(String passwordAttempt) {
        if (this.hashedPassword.equals(Authenticator.hashPassword(passwordAttempt))) {
            this.frozen = false;
            System.out.println("Account successfully unfrozen.");
        } else {
            System.out.println("Incorrect password. Unable to unfreeze account.");
        }
    }
    
    //issue a charge
    public Transaction issueCharge(User target, double amount, String description) {
        if (this.accountNumber.equals(target.accountNumber)) {
            System.out.println("You cannot charge yourself.");
            return null;
        }
        if (amount <= 0) {
            System.out.println("Charge amount invalid.");
            return null;
        }
        if (target.balance < amount) {
            System.out.println("Target user has insufficient funds.");
            return null;
        }
        target.balance -= amount;
        this.balance += amount;
        System.out.println("Charge issued successfully. You received $" + String.format("%.2f", amount));
        return new Transaction(amount, "Charge received from " + target.username + " - " + description);
    }

    //create charge record for the target user
    public Transaction issueChargeRecord(double amount, String issuerUsername, String description, String transactionID) {
        return new Transaction(-amount, "Charged by " + issuerUsername + " - " + description, transactionID);
    }    
    
    //deposit amount
    public Transaction deposit(double amount) {
    	if (amount > 0) {
            balance += amount;
            Transaction newTransaction = new Transaction(amount, "Deposit");
            System.out.println("Deposit successful. New balance: " + String.format("%.2f", balance));
            return newTransaction;
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    	return null;
    }

    //deposit silently without transaction record or printout
    public void depositSilently(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    //withdraw amount
    public Transaction withdraw(double amount) {
    	if (amount > 0 && balance >= amount) {
            balance -= amount;
            Transaction newTransaction = new Transaction(-amount, "Withdraw");
            System.out.println("Withdrawal successful. New balance: " + String.format("%.2f", balance));
            return newTransaction;
        } else if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
        } else {
            System.out.println("Insufficient balance for withdrawal.");
        }
    	return null;
    }

    //transfer money to another user
    public Transaction transferTo(User recipient, double amount, String description) {
        if (this.accountNumber.equals(recipient.accountNumber)) {
            System.out.println("You cannot transfer money to yourself.");
            return null;
        }
        if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
            return null;
        }
        if (this.balance < amount) {
            System.out.println("Insufficient funds for transfer.");
            return null;
        }
        this.balance -= amount;
        recipient.balance += amount;
        System.out.println("Transfer successful. $" + String.format("%.2f", amount) + " sent to " + recipient.getUsername());
        return new Transaction(-amount, "Transfer to " + recipient.getUsername() + " - " + description);
    }
    
    //create transfer record for the recipient
    public Transaction receiveTransfer(double amount, String senderName, String description, String transactionID) {
        return new Transaction(amount, "Transfer from " + senderName + " - " + description, transactionID);
    }
    
    public Transaction recallTransaction(Transaction pastTransaction) {
        double amount = pastTransaction.getAmount();
        String newDescription = pastTransaction.getDescription() + " [RECALLED]";

        // if (pastTransaction instanceof Loan loan) {
        //     //revert only unpaid portion
        //     amount = loan.getAmount() - loan.getAmountPaid();
        // }         

        this.balance -= amount;
        return new Transaction(-amount, newDescription);
    }   

    //Request statement
    public void printStatement(List<Transaction> transactions) {
        System.out.println("\n--- Account Statement ---");
        if (transactions != null) {
            for (Transaction t : transactions) {
                if (t != null) {
                    String extra = "";
                    if (t instanceof Loan loan) {
                        extra = String.format(" [Loan: Approved=%b, Paid=%.2f/%.2f]",
                            loan.isApproved(), loan.getAmountPaid(), loan.getAmount());
                    }
                    System.out.printf("[%s] %s: $%.2f%s\n", t.getDate(), t.getDescription(), t.getAmount(), extra);
                }
            }
        }
        System.out.printf("Current Balance: $%.2f\n", balance);
    }

    public void recieveLoanApproval(Loan loan) {
        balance += loan.getAmount();
        debt += loan.getAmount();
    }

    //Request loan
    public Loan requestLoan(double amount, String reason) {
        if (amount <= 0) {
            System.out.println("Loan amount must be positive.");
            return null;
        }
        Loan loan = new Loan(accountNumber,amount, "Loan request - " + reason);
        System.out.println("Loan request submitted for $" + String.format("%.2f", amount));
        return loan;
    }    

    //Repay loan
    public Transaction repayLoan(Loan loan, double amount) {
        if (!loan.isApproved()) {
            System.out.println("Cannot repay an unapproved loan.");
            return null;
        }
        if (loan.isPaidOff()) {
            System.out.println("This loan has already been paid off.");
            return null;
        }
        if (amount <= 0) {
            System.out.println("Invalid amount for repayment.");
            return null;
        }
        double remaining = loan.getAmount() - loan.getAmountPaid();
        if (this.balance < Math.min(amount, remaining)) {
            System.out.println("Insufficient balance for repayment.");
            return null;
        }

        double repayAmount = Math.min(amount, remaining);
        this.balance -= repayAmount;
        loan.makeRepayment(repayAmount);
        System.out.printf("Repayment successful. Repaid: $%.2f%n", repayAmount);

        return new Transaction(-repayAmount, "Loan repayment - " + loan.getDescription());
    }
    //Make payment for loan
    public Transaction makePayment(double amount, Loan loan) {
        if (amount <= 0) {
            System.out.println("Invalid payment value, it must be positive.");
        }
        if (balance >= amount) {
            balance -= amount;
            return loan.makePayment(amount);
        }
        System.out.println("Insufficient balance.");
        return null;
    }
    
}
