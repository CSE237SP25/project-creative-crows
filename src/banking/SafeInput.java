package banking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public Map<String,String> getSafeArgs(String[] args,Map<String,String> validArgs) {
        Map<String,String> presentArgs = IntStream.range(0, args.length)
            .filter(i -> args[i].startsWith("--") && validArgs.containsKey(args[i].substring(2)))
            .boxed()
            .collect(Collectors.toMap(
                i -> args[i].substring(2), i -> {                   
                    int next = i + 1;
                    return (next < args.length && !args[next].startsWith("--")) ? args[next] : validArgs.get(args[i].substring(2));
                })
            );
        // presentArgs.forEach((key,value)->System.out.println("key : "+key + ". value : "+value));
        validArgs.forEach((arg,defaultValue)->presentArgs.putIfAbsent(arg,defaultValue));
        return presentArgs;
    }
}