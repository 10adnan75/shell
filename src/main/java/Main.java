import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import core.CommandHandler;
import core.BuiltinCompleter;

public class Main {
    public static void main(String[] args) throws IOException {
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        String[] builtins = { "echo", "exit", "pwd", "cd", "type", "history" };

        while (true) {
            String input = BuiltinCompleter.readLineWithCompletion("$ ", builtins, handler.getHistory());
            if (input == null) {
                break;
            }
            input = input.trim();
            if (!input.isEmpty()) {
                currentDirectory = handler.handleCommand(input, currentDirectory);
            }
        }
    }
}