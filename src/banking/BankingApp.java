package banking;
import banking.Menu;
import banking.User;
import banking.Database;
import banking.SafeInput;
import java.util.Scanner;

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
        app.run();
    }

    private void run() {
        this.menu.run();
        System.out.println("Thanks for visiting!");
        this.keyboardInput.close();                 // close scanner
    }

}