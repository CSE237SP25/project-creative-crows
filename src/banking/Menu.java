package banking;
import banking.SafeInput;
import java.security.MessageDigest; //future class, handle reading from our files for persistence
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Menu {
    
    private Database dataHandler;
    private User activeUser;
    private List<Option> publicOptions;
    private List<Option> privateOptions;
    private boolean running;
    private SafeInput keyboardInput;

    public Menu(Database dataHandler, SafeInput keyboardInput) {
        this.dataHandler = dataHandler;
        this.keyboardInput = keyboardInput;
        this.activeUser = null;
        this.publicOptions = new ArrayList<>();
        publicOptions.add(new Option("Login to account", this::login));
        publicOptions.add(new Option("Create account", this::signUp));
        publicOptions.add(new Option("Exit",this::shutDown));
        this.privateOptions = new ArrayList<>();
        privateOptions.add(new Option("Check Balance",this::getBalance));
        privateOptions.add(new Option("View Account Number",this::getAccountNumber));
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
    
    public void getAccountNumber() {
        System.out.println("Your account number is: " + activeUser.getAccountNumber());
    }

    public void issueCharge() {
        // Since scanner returns Strings by default, just return any passed input.
        String accountNumber = keyboardInput.getSafeInput("Who would you like to charge (their account number): ","",Function.identity());

        User userToCharge = dataHandler.getUserByAccountNumber(accountNumber);

        if (userToCharge == null) {
            System.out.println("No user found with the provided account number.");
            return;
        }

        double chargeAmount = keyboardInput.getSafeInput("Charge amount: ","Invalid amount. Please enter a number.",Double::parseDouble);


        String chargeDesc = keyboardInput.getSafeInput("Charge description: ","",Function.identity());
        Transaction newTransaction = userToCharge.issueCharge(chargeAmount, chargeDesc); 
        if (newTransaction != null) {
            dataHandler.addUserTransaction(userToCharge.getUsername(), newTransaction); //add transaction history to DB
            System.out.println("Charge issued.");
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
        double amount = keyboardInput.getSafeInput("How much would you like to deposit?","Invalid amount. Please enter a number.",Double::parseDouble);
        Transaction newTransaction = activeUser.deposit(amount);
        if(newTransaction!=null) {
        	dataHandler.addUserTransaction(activeUser.getUsername(), newTransaction);
        }
        else {
        	System.out.println("Invalid amount deposited!");
        }
    }

    public void withdraw() {
        double amount = keyboardInput.getSafeInput("How much would you like to withdraw?","Invalid amount. Please enter a number.",Double::parseDouble);
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
        // passes a lambda which imposes the additional valid input range restriction. 
        int userChoice = keyboardInput.getSafeInput("Enter a number [1-"+items.size()+"]: ","Invalid selection. Please enter a number between 1 and "+items.size(), input -> {
            int value = Integer.parseInt(input);
            if (value < 1 || value > items.size()) {
                throw new IllegalArgumentException("Out of range");
            }
            return value;
        });
        i = 1;
        for (Option item : items) {
            if (i == userChoice) {
                item.execute();
            }
            i++;
        }
    }

    private void login() {
        String username = keyboardInput.getSafeInput("Enter username: ","",Function.identity());
    
        String password = keyboardInput.getSafeInput("Enter password: ","",Function.identity());
    
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
        String username = keyboardInput.getSafeInput("Enter new username: ","",Function.identity());
    
        String password = keyboardInput.getSafeInput("Enter password: ","",Function.identity());

        String passwordConfirmation = keyboardInput.getSafeInput("Confirm password: ","",Function.identity());

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