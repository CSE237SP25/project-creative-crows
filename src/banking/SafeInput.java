package banking;

import java.util.Scanner;
import java.util.function.Function;

public class SafeInput {

    private Scanner keyboardInput;
    public SafeInput(Scanner keyboardInput) {
        this.keyboardInput = keyboardInput;
    }

    public <T> T getSafeInput(String prompt, String retryPrompt, Function<String,T> parser) {
    System.out.println(prompt);
    while(true) {
        String userInput = this.keyboardInput.nextLine();
        try {
            return parser.apply(userInput);
        } catch (Exception e) {
            System.out.println(retryPrompt);
        }
    }
}
}
