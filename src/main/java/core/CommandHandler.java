package core;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import builtins.*;

public class CommandHandler {
    public static Command getCommand(String[] tokens, String rawInput, File redirectFile) {
        String cmd = tokens[0];

        return switch (cmd) {
            case "exit" -> new ExitCommand();
            case "echo" -> new EchoCommand();
            case "type" -> new TypeCommand();
            case "pwd" -> new PwdCommand();
            case "cd" -> new CdCommand();
            default -> {
                if (isExecutableAvailable(cmd)) {
                    yield new ExternalCommand(tokens, redirectFile);
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
        String[] parts = input.split("\\s+");

        File redirectFile = null;
        List<String> commandTokens = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(">") || parts[i].equals("1>")) {
                if (i + 1 < parts.length) {
                    redirectFile = new File(parts[i + 1]);
                    i++;
                } else {
                    System.err.println("Syntax error: no file specified for redirection");
                    return currentDirectory;
                }
            } else {
                commandTokens.add(parts[i]);
            }
        }

        if (commandTokens.isEmpty()) {
            return currentDirectory;
        }

        String[] cmdTokensArray = commandTokens.toArray(new String[0]);
        String rawCommand = String.join(" ", commandTokens);

        Command cmd = getCommand(cmdTokensArray, rawCommand, redirectFile);
        return cmd.execute(cmdTokensArray, rawCommand, currentDirectory);
    }
}