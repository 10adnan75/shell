package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import builtins.*;

public class CommandHandler {
    public Path handleCommand(String input, Path currentDirectory) {
        Tokenizer tokenizer = new Tokenizer();
        TokenizerResult result = tokenizer.tokenize(input);

        List<String> tokens = result.tokens;
        File redirectFile = null;

        if (result.isRedirect) {
            redirectFile = new File(result.redirectTarget);

            File parentDir = redirectFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }

        if (tokens.isEmpty()) {
            return currentDirectory;
        }

        String[] cmdTokensArray = tokens.toArray(new String[0]);
        String rawCommand = input;

        return executeCommandWithRedirection(cmdTokensArray, rawCommand, currentDirectory, redirectFile);
    }

    private Path executeCommandWithRedirection(String[] cmdTokensArray, String rawCommand,
            Path currentDirectory, File redirectFile) {
        Command cmd = getCommand(cmdTokensArray, rawCommand, redirectFile);

        boolean isBuiltin = (cmd instanceof EchoCommand || cmd instanceof CdCommand ||
                cmd instanceof ExitCommand || cmd instanceof TypeCommand ||
                cmd instanceof PwdCommand);

        PrintStream originalOut = System.out;
        try {
            if (redirectFile != null && isBuiltin) {
                System.setOut(new PrintStream(new FileOutputStream(redirectFile)));
            }

            return cmd.execute(cmdTokensArray, rawCommand, currentDirectory);
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            return currentDirectory;
        } finally {
            if (redirectFile != null && isBuiltin) {
                System.setOut(originalOut);
            }
        }
    }

    public static Command getCommand(String[] tokens, String rawInput, File redirectFile) {
        if (tokens.length == 0) {
            return new NoOpCommand();
        }

        String cmd = tokens[0];

        return switch (cmd) {
            case "exit" -> new ExitCommand();
            case "echo" -> new EchoCommand();
            case "type" -> new TypeCommand();
            case "pwd" -> new PwdCommand();
            case "cd" -> new CdCommand();
            default -> new ExternalCommand(tokens, redirectFile);
        };
    }
}