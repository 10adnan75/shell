package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import builtins.*;

public class CommandHandler {
    public static Command getCommand(String[] tokens, String rawInput) {
        String cmd = tokens[0];

        return switch (cmd) {
            case "exit" -> new ExitCommand();
            case "echo" -> new EchoCommand();
            case "type" -> new TypeCommand();
            case "pwd" -> new PwdCommand();
            case "cd" -> new CdCommand();
            default -> {
                if (isExecutableAvailable(cmd)) {
                    yield new ExternalCommand(tokens);
                } else {
                    System.out.println(rawInput + ": command not found");
                    yield new NoOpCommand();
                }
            }
        };
    }

    private static boolean isExecutableAvailable(String cmd) {
        for (String dir : System.getenv("PATH").split(":")) {
            File file = new File(dir, cmd);
            if (file.exists() && file.canExecute())
                return true;
        }
        return false;
    }

    public Path handleCommand(String input, Path currentDirectory) {
        Tokenizer tokenizer = new Tokenizer();
        TokenizerResult result = tokenizer.tokenize(input);

        PrintStream originalOut = System.out;

        try {
            if (result.isRedirect && result.redirectTarget != null) {
                FileOutputStream fos = new FileOutputStream(result.redirectTarget);
                PrintStream ps = new PrintStream(fos);
                System.setOut(ps);
            }

            Command cmd = getCommand(result.tokens.toArray(new String[0]), input);
            return cmd.execute(result.tokens.toArray(new String[0]), input, currentDirectory);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return currentDirectory;
        } finally {
            System.setOut(originalOut);
        }
    }
}