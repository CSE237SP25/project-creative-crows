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
    
    public User(String username, String hashedPassword, double balance) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.balance = balance;
    }
    public Object getHashedPassword() {
        return hashedPassword;
    }

    public String getUsername() {
        return username;
    }
    
    public double getBalance() {
        return balance;
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
    
    //Issue a charge
    public Transaction issueCharge(double amount, String description) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            Transaction newTransaction = new Transaction(-amount, description);
            return newTransaction;
        } 
        else if (amount <= 0) {
            System.out.println("Charge amount invalid.");
        } 
        else if (balance < amount) {
            System.out.println("Insufficient balance.");
        } 
        else {
            System.out.println("An error occurred. Please try again later.");
        }
        return null;
    }
    
    //deposit amount
    public Transaction deposit(double amount) {
    	if (amount > 0) {
            balance += amount;
            Transaction newTransaction = new Transaction(amount, "Deposit");
            // transactionHistory.add(newTransaction);
            System.out.println("Deposit successful. New balance: " + String.format("%.2f", balance));
            return newTransaction;
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    	return null;
    }

    //withdraw amount
    public Transaction withdraw(double amount) {
    	if (amount > 0 && balance >= amount) {
            balance -= amount;
            Transaction newTransaction = new Transaction(-amount, "Withdraw");
            // transactionHistory.add(newTransaction);
            System.out.println("Withdrawal successful. New balance: " + String.format("%.2f", balance));
            return newTransaction;
        } else if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
        } else {
            System.out.println("Insufficient balance for withdrawal.");
        }
    	return null;
    }

    //Request statement
    public void printStatement(List<Transaction> transactions) {
        System.out.println("\n--- Account Statement ---");
        if(transactions != null) {
            for (Transaction t : transactions) {
                System.out.printf("[%s] %s: $%.2f\n", 
                    t.getDate(), t.getDescription(), t.getAmount());
            }
        }
        System.out.printf("Current Balance: $%.2f\n", balance);
    }
    
}
