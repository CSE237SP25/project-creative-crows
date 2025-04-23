package banking;
import banking.Menu;
import banking.User;
import banking.Database;
import banking.SafeInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.function.Function;

public class BankingApp {
    private Scanner keyboardInput;
    private Menu menu;
    private Database dataHandler;
    private SafeInput safeInput;

    public BankingApp() {
        this.keyboardInput = new Scanner(System.in);
        this.dataHandler = new Database();
        this.safeInput = new SafeInput(keyboardInput);
        this.menu = new Menu(dataHandler, safeInput);
    }

    public static void main(String[] args) {
        BankingApp app = new BankingApp();
        Map<String,String> validArguments = new HashMap<>();
        validArguments.put("adminUser","admin");
        validArguments.put("adminPassword","supersecurepassword");
        validArguments.put("adminBalance","1000");
        // implement once loans are merged in
        validArguments.put("autoApproveLoan","false");
        Map<String,String> argMap = app.safeInput.getSafeArgs(args,validArguments);
        try {
            User admin = new User(argMap.get("adminUser"),Authenticator.hashPassword(argMap.get("adminPassword")),Double.parseDouble(argMap.get("adminBalance")));
            admin.setAuthLevel(2);
            try {
                Administrator sessionAdmin = new Administrator(app.dataHandler.createUser(admin),app.dataHandler);
                app.menu.setActiveAdmin(sessionAdmin);
                if (argMap.get("autoApproveLoan").equals("true")) app.menu.toggleAutoApprove();
                app.run();
            } finally {
                app.dataHandler.deleteUser(argMap.get("adminUser"));
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage()+": Invalid value passed to one or more of the arguments. Exiting...");
        }
    }

    private void run() {
        this.menu.run();
        System.out.println("Thanks for visiting!");
        this.keyboardInput.close();
    }

}