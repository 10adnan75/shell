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
import java.util.ArrayList;

import builtins.*;

public class CommandHandler {
    private final Map<String, Command> builtinCommands;
    private Path currentDirectory;
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final List<String> history;

    public CommandHandler() {
        this.builtinCommands = new HashMap<>();
        this.builtinCommands.put("echo", new EchoCommand());
        this.builtinCommands.put("exit", new ExitCommand());
        this.builtinCommands.put("pwd", new PwdCommand());
        this.builtinCommands.put("cd", new CdCommand());
        this.builtinCommands.put("type", new TypeCommand());
        this.history = new ArrayList<>();
        this.builtinCommands.put("history", new HistoryCommand(history));
        this.currentDirectory = Paths.get(System.getProperty("user.dir"));
        this.originalOut = System.out;
        this.originalErr = System.err;
    }

    public Path handleCommand(String input, Path currentDirectory) {
        if (!input.trim().isEmpty()) {
            history.add(input);
        }
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
        int n = pipelineParts.size();
        if (n < 2) {
            System.err.println("Pipeline must have at least two commands.");
            return currentDirectory;
        }
        try {
            Command[] commands = new Command[n];
            boolean[] isBuiltin = new boolean[n];
            boolean allExternal = true;
            for (int i = 0; i < n; i++) {
                String[] tokens = pipelineParts.get(i).tokens.toArray(new String[0]);
                commands[i] = this.getCommand(tokens, "", null, null);
                isBuiltin[i] = (commands[i] instanceof EchoCommand || commands[i] instanceof CdCommand ||
                        commands[i] instanceof ExitCommand || commands[i] instanceof TypeCommand ||
                        commands[i] instanceof PwdCommand);
                if (isBuiltin[i]) {
                    allExternal = false;
                }
            }

            if (allExternal) {
                java.util.List<ProcessBuilder> builders = new java.util.ArrayList<>();
                for (int i = 0; i < n; i++) {
                    ProcessBuilder pb = new ProcessBuilder(((ExternalCommand) commands[i]).getArgs());
                    pb.directory(currentDirectory.toFile());
                    builders.add(pb);
                }
                var processes = ProcessBuilder.startPipeline(builders);
                java.util.List<Thread> errThreads = new java.util.ArrayList<>();
                for (Process p : processes) {
                    Thread t = new Thread(() -> {
                        try (var err = p.getErrorStream()) {
                            err.transferTo(System.err);
                        } catch (Exception ignored) {
                        }
                    });
                    t.start();
                    errThreads.add(t);
                }
                try (var out = processes.get(processes.size() - 1).getInputStream()) {
                    out.transferTo(System.out);
                }
                for (Process p : processes) {
                    p.waitFor();
                }
                for (Thread t : errThreads) {
                    t.join();
                }
                return currentDirectory;
            }

            java.io.PipedInputStream[] pipeIns = new java.io.PipedInputStream[n - 1];
            java.io.PipedOutputStream[] pipeOuts = new java.io.PipedOutputStream[n - 1];
            for (int i = 0; i < n - 1; i++) {
                pipeOuts[i] = new java.io.PipedOutputStream();
                pipeIns[i] = new java.io.PipedInputStream(pipeOuts[i]);
            }

            PrintStream originalOut = System.out;
            java.io.InputStream originalIn = System.in;
            Thread[] threads = new Thread[n];
            Process[] processes = new Process[n];

            for (int i = 0; i < n; i++) {
                final int idx = i;
                if (isBuiltin[idx]) {
                    threads[idx] = new Thread(() -> {
                        PrintStream prevOut = System.out;
                        java.io.InputStream prevIn = System.in;
                        try {
                            if (idx == 0) {
                                System.setIn(originalIn);
                            } else {
                                System.setIn(pipeIns[idx - 1]);
                            }
                            if (idx == n - 1) {
                                System.setOut(originalOut);
                            } else {
                                System.setOut(new PrintStream(pipeOuts[idx], true));
                            }
                            commands[idx].execute(
                                    pipelineParts.get(idx).tokens.toArray(new String[0]),
                                    "",
                                    currentDirectory);
                            System.out.flush();
                            if (idx < n - 1) {
                                pipeOuts[idx].close();
                            }
                        } catch (Exception e) {
                        } finally {
                            System.setOut(prevOut);
                            System.setIn(prevIn);
                        }
                    });
                    threads[idx].start();
                } else {
                    ProcessBuilder pb = new ProcessBuilder(((ExternalCommand) commands[idx]).getArgs());
                    pb.directory(currentDirectory.toFile());
                    if (idx == 0) {
                        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    } else {
                        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
                    }
                    if (idx == n - 1) {
                        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    } else {
                        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    }
                    Process process = pb.start();
                    processes[idx] = process;
                    if (idx > 0) {
                        Thread t = new Thread(() -> {
                            try (var out = process.getOutputStream()) {
                                pipeIns[idx - 1].transferTo(out);
                            } catch (Exception e) {
                            }
                        });
                        t.start();
                        threads[idx] = t;
                    }
                    if (idx < n - 1) {
                        Thread t = new Thread(() -> {
                            try (var in = process.getInputStream()) {
                                in.transferTo(pipeOuts[idx]);
                                pipeOuts[idx].close();
                            } catch (Exception e) {
                            }
                        });
                        t.start();
                    }
                    Thread errThread = new Thread(() -> {
                        try (var err = process.getErrorStream()) {
                            err.transferTo(System.err);
                        } catch (Exception ignored) {
                        }
                    });
                    errThread.start();
                }
            }

            for (int i = 0; i < n; i++) {
                if (threads[i] != null) {
                    threads[i].join();
                }
                if (processes[i] != null) {
                    processes[i].waitFor();
                }
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
        Command cmd = this.getCommand(cmdTokensArray, rawCommand, redirectFile, stderrRedirectFile);

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

    public Command getCommand(String[] tokens, String rawInput, File redirectFile, File stderrRedirectFile) {
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
            case "history" -> new HistoryCommand(this.history);
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