package banking;
import java.security.MessageDigest; //future class, handle reading from our files for persistence
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    
    private Database dataHandler;
    private User activeUser;
    private List<Option> publicOptions;
    private List<Option> privateOptions;
    private Scanner keyboardInput;
    private boolean running;


    public Menu(Scanner keyboardInput, Database dataHandler) {
        this.dataHandler = dataHandler;
        this.keyboardInput = keyboardInput;
        this.activeUser = null;
        this.publicOptions = new ArrayList<>();
        publicOptions.add(new Option("Login to account", this::login));
        publicOptions.add(new Option("Create account", this::signUp));
        publicOptions.add(new Option("Exit",this::shutDown));
        this.privateOptions = new ArrayList<>();
        privateOptions.add(new Option("Check Balance",this::getBalance));
        privateOptions.add(new Option("Deposit",this::deposit));
        privateOptions.add(new Option("Withdraw",this::withdraw));
        privateOptions.add(new Option("Issue Charge",this::issueCharge));
        privateOptions.add(new Option("Print Statement",this::printStatement));
        privateOptions.add(new Option("Logout",this::logOut));
        this.running = false;
    }
    
    public Database getDataHandler() {
    	return dataHandler;
    }

    public void run() {
        this.running = true;
        while (this.running) {
            printScopedMenu();
        }
    }

    public void shutDown() {
        this.running = false;
    }

    public void printScopedMenu() {
        if (this.activeUser != null) {
            this.printMenu(privateOptions);
        } else {
            this.printMenu(publicOptions);
        }
    }

    public void getBalance() {
        System.out.printf("Your balance is currently: %.2f%n", activeUser.getBalance());//added precision for double
    }

    public void issueCharge() {
        System.out.print("Who would you like to charge (their user_id): ");
        String nameOfUserToCharge = keyboardInput.nextLine();

        if(!dataHandler.doesUserExist(nameOfUserToCharge)) {
        	System.out.println("No such user");
        	return;
        }

        // Reminder to user that they are charging themselves
        if (nameOfUserToCharge.equals(activeUser.getUsername())) {
            System.out.println("Note: This is a self-charge (service fee).");
        }

        User userToCharge = dataHandler.getUserData(nameOfUserToCharge);
        System.out.print("Charge amount: ");
        double chargeAmount;
        try {
            chargeAmount = keyboardInput.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Please enter a number.");
            keyboardInput.nextLine();
            return;
        }

        keyboardInput.nextLine();
        System.out.print("Charge description: ");
        String chargeDesc = keyboardInput.nextLine();

        // Self-Charge
        if (userToCharge.getUsername().equals(activeUser.getUsername())) {
            Transaction selfCharge = activeUser.issueCharge(chargeAmount, "Self Charge: " + chargeDesc);
            if (selfCharge != null) {
                dataHandler.addUserTransaction(activeUser.getUsername(), selfCharge);
                System.out.println("Self charge issued.");
            }
        } 
        else {
            // Merchant charges customer
            Transaction customerTransaction = userToCharge.issueCharge(chargeAmount, "Charge from " + activeUser.getUsername() + ": " + chargeDesc);
            if (customerTransaction == null) {
                System.out.println("Charge failed.");
                return;
            }
            System.out.println("Charge issued successfully.");
            
            dataHandler.addUserTransaction(userToCharge.getUsername(), customerTransaction);
            activeUser.deposit(chargeAmount);
            Transaction merchantTransaction = new Transaction(chargeAmount, "Received from " + userToCharge.getUsername() + ": " + chargeDesc);
            dataHandler.addUserTransaction(activeUser.getUsername(), merchantTransaction);
        }
    }

    public void printStatement() {
        if(activeUser != null) {
            List<Transaction> transactions = dataHandler.getUserTransaction(
                activeUser.getUsername()
            );
            activeUser.printStatement(transactions);
        }
    }

    public void deposit() {
        System.out.println("How much would you like to deposit?");
        double amount;
        try {
            amount = keyboardInput.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Please enter a number.");
            keyboardInput.nextLine();
            return;
        }
        Transaction newTransaction = activeUser.deposit(amount);
        if(newTransaction!=null) {
        	dataHandler.addUserTransaction(activeUser.getUsername(), newTransaction);
        }
        else {
        	System.out.println("Invalid amount deposited!");
        }
    }

    public void withdraw() {
        System.out.println("How much would you like to withdraw?");
        double amount;
        try {
            amount = keyboardInput.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Please enter a number.");
            keyboardInput.nextLine();
            return;
        }
        Transaction newTransaction = activeUser.withdraw(amount);
        if (newTransaction!=null) {
            dataHandler.addUserTransaction(activeUser.getUsername(), newTransaction);
        }
    }

    public void printMenu(List<Option> items) {
        int i = 1;
        for (Option item : items) {
            System.out.println(i + ". " + item.getOptionName());
            i++;
        }
        int userChoice = keyboardInput.nextInt();
        i = 1;
        for (Option item : items) {
            if (i == userChoice) {
                keyboardInput.nextLine(); // clears the newline from the item selection
                item.execute();
            }
            i++;
        }
    }

    private void login() {
        System.out.println("Enter username: ");
        String username = keyboardInput.nextLine();
    
        System.out.println("Enter password: ");
        String password = keyboardInput.nextLine();
    
        if (dataHandler.doesUserExist(username)) {
            if(authenticateUserPass(username, password)) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Login failed: password hashes do not match");
            }
        } else {
            System.out.println("Login failed: user does not exist");
        }
    }

    public boolean authenticateUserPass(String username,String password) {
        User requestedAccount = dataHandler.getUserData(username);
        if (requestedAccount.getHashedPassword().equals(Menu.hashPassword(password))) {
            this.activeUser = requestedAccount;
            return true;
        }
        return false;
    }
    
    public void signUp() {
        System.out.println("Enter new username: ");
        String username = keyboardInput.nextLine();

    
        System.out.println("Enter password: ");
        String password = keyboardInput.nextLine();

        System.out.println("Confirm password: ");
        String passwordConfirmation = keyboardInput.nextLine();

        if(!password.equals(passwordConfirmation)) {
            System.out.println("Passwords do not match, exiting...");
        } else {
            if (createUser(username, passwordConfirmation, 0)) {
                System.out.println("Account created successfully!");
            } else {
                System.out.println("Account already exists.");
            }
        }
    }


    public boolean createUser(String username, String password, double balance) {
        if (!dataHandler.doesUserExist(username)) {
            User userToRegister = new User(username, Menu.hashPassword(password), balance);
            this.activeUser = dataHandler.createUser(userToRegister);
            return true;
        }
        return false;
    }

    public void logOut() {
        this.activeUser = null;
        System.out.println("Logged out Succesfully");
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            // Convert hash bytes to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            String hashedPassword = sb.toString();
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.");
        }
    }
    
    public User getActiveUser() {
    	return activeUser;
    }

}