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
        if (pipelineParts.size() != 2) {
            System.err.println("Only pipelines of two external commands are supported.");
            return currentDirectory;
        }
        try {
            TokenizerResult part1 = pipelineParts.get(0);
            String[] cmdTokensArray1 = part1.tokens.toArray(new String[0]);
            Command cmd1 = getCommand(cmdTokensArray1, "", null, null);
            if (!(cmd1 instanceof ExternalCommand)) {
                System.err.println("Pipeline only supports external commands");
                return currentDirectory;
            }
            ProcessBuilder pb1 = new ProcessBuilder(((ExternalCommand) cmd1).getArgs());
            pb1.directory(currentDirectory.toFile());

            TokenizerResult part2 = pipelineParts.get(1);
            String[] cmdTokensArray2 = part2.tokens.toArray(new String[0]);
            Command cmd2 = getCommand(cmdTokensArray2, "", null, null);
            if (!(cmd2 instanceof ExternalCommand)) {
                System.err.println("Pipeline only supports external commands");
                return currentDirectory;
            }
            ProcessBuilder pb2 = new ProcessBuilder(((ExternalCommand) cmd2).getArgs());
            pb2.directory(currentDirectory.toFile());

            var processes = ProcessBuilder.startPipeline(java.util.List.of(pb1, pb2));
            Process p1 = processes.get(0);
            Process p2 = processes.get(1);

            Thread p1ErrThread = new Thread(() -> {
                try (var err = p1.getErrorStream()) {
                    err.transferTo(System.err);
                } catch (Exception ignored) {
                }
            });
            p1ErrThread.start();
            Thread p2ErrThread = new Thread(() -> {
                try (var err = p2.getErrorStream()) {
                    err.transferTo(System.err);
                } catch (Exception ignored) {
                }
            });
            p2ErrThread.start();

            try (var out = p2.getInputStream()) {
                out.transferTo(System.out);
            }

            int p2Exit = p2.waitFor();
            if (p1.isAlive()) {
                p1.destroy();
            }
            p1.waitFor();
            p1ErrThread.join();
            p2ErrThread.join();
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