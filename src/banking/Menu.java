package banking;
import java.util.ArrayList;
import java.util.HashMap; //future class, handle reading from our files for persistence
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.function.BooleanSupplier;

import banking.Authenticator;
import banking.QRCodeGenerator;
import java.util.Map;
import java.util.function.Function;

public class Menu {
    
    private Database dataHandler;
    private User activeUser;
    private List<Option> publicOptions;
    private List<Option> privateOptions;
    private List<Option> adminOptions;
    private boolean running;
    private SafeInput keyboardInput;
    private User subsystemUser;
    private String menuScope = "public";
    private User rootUser;
    private Administrator rootAdmin;
    
    private void initializeRootAdmin() {
        this.rootUser = new User("admin", Authenticator.hashPassword(""), 0.0);
        this.rootAdmin = new Administrator(rootUser);
        this.rootAdmin.setAuthLevel(2);
        if (!dataHandler.doesUserExist("admin")) {
            dataHandler.createUser(rootAdmin);
        }
    }

    private void initializePublicOptions() {
        this.publicOptions = new ArrayList<>();
        publicOptions.add(new Option("Login to account", this::login));
        publicOptions.add(new Option("Create account", this::signUp));
        publicOptions.add(new Option("Reset Password", this::recoverAccount));
        publicOptions.add(new Option("Exit", this::shutDown));
    }

    private void initializePrivateOptions() {
        this.privateOptions = new ArrayList<>();
        privateOptions.add(new Option("Check Balance", this::getBalance));
        privateOptions.add(new Option("View Account Number", this::getAccountNumber));
        privateOptions.add(new Option("Deposit", this::deposit));
        privateOptions.add(new Option("Withdraw", this::withdraw));
        privateOptions.add(new Option("Transfer Money", this::transferMoney));
        privateOptions.add(new Option("Issue Charge", this::issueCharge));
        privateOptions.add(new Option("Print Statement", this::printStatement));
        privateOptions.add(new Option("Request Loan", this::requestLoan));
        privateOptions.add(new Option("Repay Loan", this::repayLoan));
        privateOptions.add(new Option("Change Password", this::changePassword));
        privateOptions.add(new Option("Enable 2FA Recovery", this::enable2FA, () -> activeUser.getSecret() == null));
        privateOptions.add(new Option("Remove 2FA Recovery", this::remove2FA, () -> activeUser.getSecret() != null));
        privateOptions.add(new Option("Change Username", this::changeUsername));
        privateOptions.add(new Option("Logout", this::logOut));
    }

    private void initializeAdminOptions() {
        this.adminOptions = new ArrayList<>();
        adminOptions.add(new Option("Print All Transaction", this::printAllTransactions));
        adminOptions.add(new Option("Recall Transaction", this::recallTransaction));
        adminOptions.add(new Option("Review All Loans", this::adminReviewLoans));
        adminOptions.add(new Option("Logout", this::logOut));
    }


    public Menu(Database dataHandler, SafeInput keyboardInput) {
        this.dataHandler = dataHandler;
        this.keyboardInput = keyboardInput;
        this.subsystemUser = new User("__MENU_SUBSYSTEM__","0",0);
        this.activeUser = subsystemUser;
        this.running = false;

        initializeRootAdmin();
        initializePublicOptions();
        initializePrivateOptions();
        initializeAdminOptions();
    }

    
    public Database getDataHandler() {
    	return dataHandler;
    }

    public void enableAdminView() {
        this.menuScope = "admin";
    }

    public User getSubsystemUser() {
        return this.subsystemUser;
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
        // this still needs to be seperate from the authorization system, otherwise we run into later options calling a method on null
        switch (this.menuScope) {
            case "private":
                this.printMenu(privateOptions);
            break;
            case "admin":
                this.printMenu(adminOptions);
            break;
            default:
                this.printMenu(publicOptions);
            break;
        }
    }

    public void printMenu(List<Option> items) {
        List<Option> visibleItems = items.stream().filter(Option::isVisible).collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger(1);
        List<String> labels = visibleItems.stream().map(opt -> counter.getAndIncrement() + ". " + opt.getOptionName()).collect(Collectors.toList());
        // sets the colWidth to the length of the maximum label + padding (=4)
        int colWidth = labels.stream().mapToInt(String::length).max().orElse(0) + 4;   
        // prints a 2 column menu
        IntStream.range(0, labels.size()).filter(i -> i % 2 == 0).forEach(i -> {
                 String left  = labels.get(i);
                 String right = (i + 1 < labels.size()) ? labels.get(i + 1) : "";
                 // %-colWidth string -> "Print string using Left Justify, taking a minimum space of colWidth"
                 System.out.printf("%-" + colWidth + "s%s%n", left, right);
             });
        // passes a lambda which imposes the additional valid input range restriction. 
        int userChoice = keyboardInput.getSafeInput("Enter a number [1-"+visibleItems.size()+"]: ","Invalid selection. Please enter a number between 1 and "+visibleItems.size(), input -> {
            int value = Integer.parseInt(input);
            if (value < 1 || value > visibleItems.size()) throw new IllegalArgumentException("Out of range");
            return value;
        });
        // executes the chosen option
        visibleItems.get(userChoice-1).execute();
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
        Transaction issuerTransaction = activeUser.issueCharge(userToCharge, chargeAmount, chargeDesc);
        if (issuerTransaction != null) {
            dataHandler.addUserTransaction(activeUser.getUsername(), issuerTransaction);
            dataHandler.addUserTransaction(userToCharge.getUsername(), userToCharge.issueChargeRecord(chargeAmount, activeUser.getUsername(), chargeDesc, issuerTransaction.getTransactionID()));
        }
        else {
            System.out.println("Charge failed.");
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

    public void transferMoney() {
        String accountNumber = keyboardInput.getSafeInput("Enter recipient's account number:", "", Function.identity());
        User recipient = dataHandler.getUserByAccountNumber(accountNumber);
    
        if (recipient == null) {
            System.out.println("No user found with that account number.");
            return;
        }
    
        double amount = keyboardInput.getSafeInput("Enter amount to transfer:", "Invalid amount. Please enter a number.", Double::parseDouble);
        String description = keyboardInput.getSafeInput("Enter transfer description:", "", Function.identity());
    
        Transaction senderTransaction = activeUser.transferTo(recipient, amount, description);
        if (senderTransaction != null) {
        	String thisTransactionID = senderTransaction.getTransactionID();
            Transaction recipientTransaction = recipient.receiveTransfer(amount, activeUser.getUsername(), description, thisTransactionID);
            dataHandler.addUserTransaction(activeUser.getUsername(), senderTransaction);
            dataHandler.addUserTransaction(recipient.getUsername(), recipientTransaction);
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
        if (requestedAccount.getHashedPassword().equals(Authenticator.hashPassword(password))) {
        	if (requestedAccount.isAuthorizedFor(2)) {
        		this.menuScope = "admin";
        	}else {
        		this.menuScope = "private";
        	}
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

    public void enable2FA() {
        String userSecret = Authenticator.generateSecureSecret();
        this.activeUser.setSecret(userSecret);
        System.out.println("Add the following code to your 2FA application (authy, google authenticatr, etc): "+userSecret);
        QRCodeGenerator.printQRCodeFromSecret(this.activeUser.getUsername(),this.activeUser.getSecret());
        dataHandler.updateUserInfo();
    }

    public void remove2FA() {
        this.activeUser.setSecret(null);
        System.out.println("Your 2FA has been removed. Tread carefully...");
    }

    public void recoverAccount() {
        String username = keyboardInput.getSafeInput("Please enter the username of the account you want to reset: ","",Function.identity());
        if (!dataHandler.doesUserExist(username)) {
            System.out.println("Username does not exist.");
            return;
        }
        User requestedUser = dataHandler.getUserData(username);
        if (requestedUser.getSecret() == null) {
            System.out.println("You have not enabled 2FA.");
            return;
        }
        int currentCode = keyboardInput.getSafeInput("Please enter your 6 digit 2FA code","Please enter a valid 6 digit code",input->{
            if (input.length() == 6) return Integer.parseInt(input);
            else throw new IllegalArgumentException("Invalid Length");
        });
        if (Authenticator.validateTOTP(requestedUser.getSecret(),currentCode)) {
            resetPassword(requestedUser);
        } else {
            System.out.println("Invalid 2FA code.");
        }
    }

    public void resetPassword(User target) {
        String password = keyboardInput.getSafeInput("Enter new password: ","",Function.identity());
        String passwordConfirmation = keyboardInput.getSafeInput("Confirm password: ","",Function.identity());
        if(!password.equals(passwordConfirmation)) {
            System.out.println("Passwords do not match, exiting...");
        } else {
            target.resetPassword(Authenticator.hashPassword(password));
            System.out.println("Your password has been succesfully reset!");
        }
    }

    public void changePassword() {
        // just a wrapper since the option class takes in a function with no inputs or return type.
        resetPassword(this.activeUser);
        dataHandler.updateUserInfo();
    }
    
    public void changeUsername() {
        String newUsername = keyboardInput.getSafeInput("Enter new username: ", "", Function.identity());

        // checking if the new username is already taken
        if (dataHandler.doesUserExist(newUsername)) {
            System.out.println("Username already taken. Please choose another one.");
            return;
        }

        String oldUsername = activeUser.getUsername();

        activeUser.changeUsername(newUsername);
        dataHandler.updateUsername(oldUsername, newUsername, activeUser);

        System.out.println("Username changed successfully to: " + newUsername);
    }

    public boolean createUser(String username, String password, double balance) {
        if (!dataHandler.doesUserExist(username)) {
            User userToRegister = new User(username, Authenticator.hashPassword(password), balance);
            this.activeUser = dataHandler.createUser(userToRegister);
            this.menuScope = "private";
            return true;
        }
        return false;
    }
    

    public void recallTransaction() {
        String transactionID = keyboardInput.getSafeInput("Which transaction would you like to recall? (type transaction id): ","",Function.identity());
        boolean idExists = dataHandler.getAllTransactions().stream().anyMatch(t -> t.getTransactionID().equals(transactionID));
        HashMap<User, Transaction> usersInfluenced = dataHandler.recallTransaction(transactionID);

        if (usersInfluenced.isEmpty()) {
            //match found but recall was not allowed by rules, do nothing
            if (idExists) {
                return;
            } 
            //no match found, return error message
            else {
                System.out.println("No matching transaction found for the given ID!");
                return;
            }
        }
        new Administrator(this.activeUser).recallTransactions(usersInfluenced);
        dataHandler.updateUserInfo();
        dataHandler.saveAllTransactions();
    }

    public void printAllTransactions() {
        List<Transaction> transactionList = dataHandler.getAllTransactions();
        new Administrator(this.activeUser).printAllTransactions(transactionList);
    }

    public void requestLoan() {
        double amount = keyboardInput.getSafeInput("Loan amount:", "Invalid amount. Please enter a number.", Double::parseDouble);
        String reason = keyboardInput.getSafeInput("Loan purpose:", "", Function.identity());
        Loan loan = activeUser.requestLoan(amount, reason);
        if (loan != null) {
            dataHandler.addUserTransaction(activeUser.getUsername(), loan);
            System.out.println("Awaiting admin approval.");
        }
    }

    public void repayLoan() {
        List<Transaction> userTx = dataHandler.getUserTransaction(activeUser.getUsername());
        System.out.println("\n--- Your Loans ---");
        for (Transaction t : userTx) {
            if (t instanceof Loan loan) {
                System.out.printf("%s: $%.2f [Approved=%b, Paid=%.2f/%.2f] [ID: %s]\n",
                    loan.getDescription(), loan.getAmount(), loan.isApproved(),
                    loan.getAmountPaid(), loan.getAmount(), loan.getTransactionID());
            }
        }
    
        String loanID = keyboardInput.getSafeInput("Enter Loan ID to repay (or type 'exit' to exit):", "", Function.identity());
        if (loanID.equalsIgnoreCase("exit")) {
            return;
        }
        Loan selectedLoan = null;
        for (Transaction t : userTx) {
            if (t instanceof Loan loan && loan.getTransactionID().equals(loanID)) {
                selectedLoan = loan;
                break;
            }
        }
        if (selectedLoan == null) {
            System.out.println("No matching loan found for the given ID.");
            return;
        }
    
        double repayAmount = keyboardInput.getSafeInput("Enter amount to repay:", "Invalid amount. Please enter a number.", Double::parseDouble);
        Transaction repayment = activeUser.repayLoan(selectedLoan, repayAmount);
        if (repayment != null) {
            dataHandler.addUserTransaction(activeUser.getUsername(), repayment);
            dataHandler.updateUserInfo();
            dataHandler.saveAllTransactions();
        }
    }
    
    
    public void adminReviewLoans() {
        Map<String, List<Transaction>> allTransactions = dataHandler.getAllTransactionMap();
        Administrator admin = new Administrator(activeUser);
        admin.showAllLoansWithStatus(allTransactions);

        String loanIdToApprove = keyboardInput.getSafeInput("\nEnter the Loan ID to approve (or type 'exit' to exit):", "Invalid input.", Function.identity());

        if (!loanIdToApprove.equalsIgnoreCase("exit")) {
            boolean approved = admin.approveLoanById(loanIdToApprove, allTransactions, dataHandler);
            if (approved) {
                dataHandler.updateUserInfo();
                dataHandler.saveAllTransactions();
            }
        }
    }

    public void logOut() {
        this.activeUser = subsystemUser;
        this.menuScope = "public";
        System.out.println("Logged out Succesfully");
    }
    
    
    public User getActiveUser() {
    	return activeUser;
    }

}