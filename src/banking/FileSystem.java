package banking;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystem {

    private File userFile;
    private File transactionFile;

    public FileSystem() {
        this.userFile = new File("userMap.ser");
        this.transactionFile = new File("transactionMap.ser");
    }
    
    public FileSystem(String customizedUserFile, String customizedTransactionFile) {
        this.userFile = new File(customizedUserFile);
        this.transactionFile = new File(customizedTransactionFile);
    }


    // Save users to file
    public void saveUsersToFile(Map<String, User> userMap) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFile))) {
            oos.writeObject(userMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load users from file
    public Map<String, User> loadUsersFromFile() {
        if (!userFile.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFile))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
 // Save transactions to file
    public void saveTransactionsToFile(Map<String, List<Transaction>> transactionMap) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(transactionFile))) {
            oos.writeObject(transactionMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load transactions from file
    public Map<String, List<Transaction>> loadTransactionsFromFile() {
        if (!transactionFile.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(transactionFile))) {
            return (Map<String, List<Transaction>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
