package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;

import builtins.*;

public class CommandHandler {
    private final Map<String, Command> builtinCommands;
    private Path currentDirectory;
    private final PrintStream originalOut;
    private final PrintStream originalErr;

    public CommandHandler() {
        this.builtinCommands = new HashMap<>();
        this.builtinCommands.put("echo", new EchoCommand());
        this.builtinCommands.put("exit", new ExitCommand());
        this.builtinCommands.put("pwd", new PwdCommand());
        this.builtinCommands.put("cd", new CdCommand());
        this.builtinCommands.put("type", new TypeCommand());
        this.currentDirectory = Paths.get(System.getProperty("user.dir"));
        this.originalOut = System.out;
        this.originalErr = System.err;
    }

    public Path handleCommand(String input, Path currentDirectory) {
        Tokenizer tokenizer = new Tokenizer();
        TokenizerResult result = tokenizer.tokenize(input);

        if (result.isPipeline) {
            return handlePipeline(result.pipelineParts, currentDirectory);
        }

        List<String> tokens = result.tokens;
        File redirectFile = null;
        File stderrRedirectFile = null;

        if (result.isRedirect) {
            redirectFile = new File(result.redirectTarget);
            File parentDir = redirectFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!redirectFile.exists()) {
                try {
                    redirectFile.createNewFile();
                } catch (Exception e) {
                    System.err.println("Error creating redirect file: " + e.getMessage());
                }
            }
        }

        if (result.isStderrRedirect) {
            stderrRedirectFile = new File(result.stderrRedirectTarget);
            File parentDir = stderrRedirectFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                try {
                    parentDir.mkdirs();
                } catch (Exception e) {
                    System.err.println("Error creating stderr redirect directory: " + e.getMessage());
                    return currentDirectory;
                }
            }
            if (!stderrRedirectFile.exists()) {
                try {
                    stderrRedirectFile.createNewFile();
                } catch (Exception e) {
                    System.err.println("Error creating stderr redirect file: " + e.getMessage());
                    return currentDirectory;
                }
            }
        }

        if (tokens.isEmpty()) {
            return this.currentDirectory;
        }

        String[] cmdTokensArray = tokens.toArray(new String[0]);
        String rawCommand = input;

        return executeCommandWithRedirection(cmdTokensArray, rawCommand, currentDirectory, redirectFile,
                stderrRedirectFile, result.isAppend, result.isStderrAppend);
    }

    private Path handlePipeline(List<TokenizerResult> pipelineParts, Path currentDirectory) {
        try {
            ProcessBuilder[] processBuilders = new ProcessBuilder[pipelineParts.size()];
            Process[] processes = new Process[pipelineParts.size()];

            for (int i = 0; i < pipelineParts.size(); i++) {
                TokenizerResult part = pipelineParts.get(i);
                String[] cmdTokensArray = part.tokens.toArray(new String[0]);
                Command cmd = getCommand(cmdTokensArray, "", null, null);

                if (cmd instanceof ExternalCommand) {
                    processBuilders[i] = new ProcessBuilder(((ExternalCommand) cmd).getArgs());
                    processBuilders[i].directory(currentDirectory.toFile());
                } else {
                    System.err.println("Pipeline only supports external commands");
                    return currentDirectory;
                }
            }

            for (int i = 0; i < processBuilders.length; i++) {
                processes[i] = processBuilders[i].start();
            }

            for (int i = 0; i < processes.length - 1; i++) {
                final int index = i;
                try (var out = processes[index].getInputStream();
                        var in = processes[index + 1].getOutputStream()) {
                    out.transferTo(in);
                }
            }

            try (var out = processes[processes.length - 1].getInputStream()) {
                out.transferTo(System.out);
            }

            for (Process process : processes) {
                try (var err = process.getErrorStream()) {
                    err.transferTo(System.err);
                }
            }

            processes[processes.length - 1].waitFor();

            for (int i = 0; i < processes.length - 1; i++) {
                processes[i].waitFor();
            }

            return currentDirectory;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing pipeline: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return currentDirectory;
        }
    }

    private Path executeCommandWithRedirection(String[] cmdTokensArray, String rawCommand,
            Path currentDirectory, File redirectFile, File stderrRedirectFile, boolean isAppend,
            boolean isStderrAppend) {
        Command cmd = getCommand(cmdTokensArray, rawCommand, redirectFile, stderrRedirectFile);

        boolean isBuiltin = (cmd instanceof EchoCommand || cmd instanceof CdCommand ||
                cmd instanceof ExitCommand || cmd instanceof TypeCommand ||
                cmd instanceof PwdCommand);

        try {
            if (redirectFile != null && isBuiltin) {
                FileOutputStream fos = new FileOutputStream(redirectFile, isAppend);
                PrintStream ps = new PrintStream(fos, true) {
                    @Override
                    public void print(String s) {
                        if (!s.equals("$ ")) {
                            super.print(s);
                        }
                    }

                    @Override
                    public void println(String s) {
                        if (!s.equals("$ ")) {
                            super.println(s);
                        }
                    }
                };
                System.setOut(ps);
            }

            if (stderrRedirectFile != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(stderrRedirectFile, isStderrAppend);
                    PrintStream ps = new PrintStream(fos, true) {
                        @Override
                        public void print(String s) {
                            if (!s.equals("$ ")) {
                                super.print(s);
                            }
                        }

                        @Override
                        public void println(String s) {
                            if (!s.equals("$ ")) {
                                super.println(s);
                            }
                        }
                    };
                    System.setErr(ps);
                } catch (IOException e) {
                    System.err.println("Error setting up stderr redirection: " + e.getMessage());
                    return currentDirectory;
                }
            }

            Path result = cmd.execute(cmdTokensArray, rawCommand, currentDirectory);

            if (redirectFile != null && isBuiltin) {
                System.out.flush();
            }
            if (stderrRedirectFile != null) {
                System.err.flush();
            }

            return result;
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            return this.currentDirectory;
        } finally {
            if (redirectFile != null && isBuiltin) {
                System.setOut(originalOut);
            }
            if (stderrRedirectFile != null) {
                System.setErr(originalErr);
            }
        }
    }

    public static Command getCommand(String[] tokens, String rawInput, File redirectFile, File stderrRedirectFile) {
        if (tokens.length == 0) {
            return new NoOpCommand();
        }

        String cmd = tokens[0];
        String cmdUnquoted = cmd;
        if ((cmd.startsWith("\"") && cmd.endsWith("\"")) ||
                (cmd.startsWith("'") && cmd.endsWith("'"))) {
            cmdUnquoted = cmd.substring(1, cmd.length() - 1);
        }

        return switch (cmdUnquoted) {
            case "exit" -> new ExitCommand();
            case "echo" -> new EchoCommand();
            case "type" -> new TypeCommand();
            case "pwd" -> new PwdCommand();
            case "cd" -> new CdCommand();
            default -> {
                String path = getPath(cmdUnquoted);
                if (path == null) {
                    System.err.printf("%s: command not found%n", cmd);
                    yield new NoOpCommand();
                }
                tokens[0] = cmdUnquoted;
                for (int i = 0; i < tokens.length; i++) {
                    if ((tokens[i].startsWith("\"") && tokens[i].endsWith("\"")) ||
                            (tokens[i].startsWith("'") && tokens[i].endsWith("'"))) {
                        tokens[i] = tokens[i].substring(1, tokens[i].length() - 1);
                    }
                }
                yield new ExternalCommand(Arrays.asList(tokens), redirectFile, stderrRedirectFile);
            }
        };
    }

    private static String getPath(String command) {
        Path cmdPath = Path.of(command);
        if ((cmdPath.isAbsolute() || command.contains("/")) &&
                Files.isRegularFile(cmdPath) && Files.isExecutable(cmdPath)) {
            return cmdPath.toString();
        }
        for (String path : System.getenv("PATH").split(":")) {
            Path fullPath = Path.of(path, command);
            if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {
                return fullPath.toString();
            }
        }
        Path cwdPath = Path.of(System.getProperty("user.dir")).resolve(command);
        if (Files.exists(cwdPath) && Files.isExecutable(cwdPath)) {
            return cwdPath.toAbsolutePath().toString();
        }
        return null;
    }
}