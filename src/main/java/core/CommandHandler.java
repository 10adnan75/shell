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
        }

        if (tokens.isEmpty()) {
            return currentDirectory;
        }

        String[] cmdTokensArray = tokens.toArray(new String[0]);
        String rawCommand = input;
        String command = cmdTokensArray[0];
        Command cmd = getCommand(cmdTokensArray, rawCommand, redirectFile);

        PrintStream originalOut = System.out;
        try {
            if (redirectFile != null) {
                File parentDir = redirectFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                boolean isBuiltin = (cmd instanceof EchoCommand || cmd instanceof CdCommand ||
                        cmd instanceof ExitCommand || cmd instanceof TypeCommand ||
                        cmd instanceof PwdCommand);

                if (isBuiltin) {
                    System.setOut(new PrintStream(new FileOutputStream(redirectFile)));
                }
            }

            return cmd.execute(cmdTokensArray, rawCommand, currentDirectory);
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            return currentDirectory;
        } finally {
            if (redirectFile != null && (cmd instanceof EchoCommand || cmd instanceof CdCommand ||
                    cmd instanceof ExitCommand || cmd instanceof TypeCommand ||
                    cmd instanceof PwdCommand)) {
                System.setOut(originalOut);
            }
        }
    }

    public static Command getCommand(String[] tokens, String rawInput, File redirectFile) {
        if (tokens.length == 0) {
            return new NoOpCommand();
        }

        String cmd = tokens[0];

        if (cmd.startsWith("\"") && cmd.endsWith("\"")) {
            String executableName = cmd.substring(1, cmd.length() - 1);
            File file = new File(executableName);

            if (file.exists() && file.canExecute()) {
                String[] newTokens = new String[tokens.length];
                newTokens[0] = executableName;
                System.arraycopy(tokens, 1, newTokens, 1, tokens.length - 1);
                return new ExternalCommand(newTokens, redirectFile);
            }
        }

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
                    File file = new File(cmd);
                    if (file.exists() && file.canExecute()) {
                        yield new ExternalCommand(tokens, redirectFile);
                    } else {
                        System.out.println(rawInput + ": command not found");
                        yield new NoOpCommand();
                    }
                }
            }
        };
    }

    private static boolean isExecutableAvailable(String cmd) {
        if (cmd.startsWith("\"") && cmd.endsWith("\"")) {
            cmd = cmd.substring(1, cmd.length() - 1);
        }

        for (String dir : System.getenv("PATH").split(":")) {
            File file = new File(dir, cmd);
            if (file.exists() && file.canExecute())
                return true;
        }
        return false;
    }
}