package core;

import java.io.File;
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
import java.io.InputStream;

import builtins.*;

public class CommandHandler {
    private final Map<String, Command> builtinCommands;
    private Path currentDirectory;
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
    }

    public Path handleCommand(String input, Path currentDirectory) {
        if (!input.trim().isEmpty()) {
            history.add(input);
        }

        if (input.contains("|")) {
            String[] pipelineSegments = input.split("\\|");
            List<String> parts = new ArrayList<>();
            for (String segment : pipelineSegments) {
                parts.add(segment.trim());
            }
            ExtractResult extractResult = extractStreams(tokenize(input));
            Streams streams = extractResult.streams;
            handlePipeline(parts, streams, currentDirectory);
            return this.currentDirectory;
        }
        List<String> tokens = tokenize(input);
        if (tokens.isEmpty()) {
            return this.currentDirectory;
        }
        ExtractResult extractResult = extractStreams(tokens);
        List<String> partsList = extractResult.commands;
        Streams streams = extractResult.streams;
        if (partsList.isEmpty())
            return this.currentDirectory;
        String command = partsList.get(0);
        List<String> arguments = partsList.subList(1, partsList.size());
        if (command.equals("exit")) {
            int exitCode = 0;
            if (!arguments.isEmpty()) {
                try {
                    exitCode = Integer.parseInt(arguments.get(0));
                } catch (NumberFormatException e) {
                    exitCode = 0;
                }
            }
            System.exit(exitCode);
        }
        if (builtinCommands.containsKey(command)) {
            Command builtin = builtinCommands.get(command);
            String[] cmdArgs = new String[1 + arguments.size()];
            cmdArgs[0] = command;
            for (int i = 0; i < arguments.size(); i++) {
                cmdArgs[i + 1] = arguments.get(i);
            }
            builtin.execute(cmdArgs, input, currentDirectory);
            return this.currentDirectory;
        } else {
            handleExternalCommand(partsList, streams, currentDirectory);
            return this.currentDirectory;
        }
    }

    private List<String> tokenize(String input) {
        var result = new ArrayList<String>();
        var currentToken = new StringBuilder();
        Character quote = null;
        boolean backslash = false;
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (c) {
                case ' ' -> {
                    if (backslash) {
                        currentToken.append(c);
                    } else if (quote == null) {
                        if (!currentToken.isEmpty()) {
                            result.add(currentToken.toString());
                            currentToken = new StringBuilder();
                        }
                    } else {
                        currentToken.append(c);
                    }
                }
                case '|' -> {
                    if (backslash) {
                        currentToken.append(c);
                    } else if (quote == null) {
                        if (!currentToken.isEmpty()) {
                            result.add(currentToken.toString());
                            currentToken = new StringBuilder();
                        }
                        result.add("|");
                    } else {
                        currentToken.append(c);
                    }
                }
                case '\'', '"' -> {
                    if (backslash) {
                        currentToken.append(c);
                    } else {
                        if (quote == null) {
                            quote = c;
                        } else {
                            if (quote.equals(c)) {
                                quote = null;
                            } else {
                                currentToken.append(c);
                            }
                        }
                    }
                }
                case '\\' -> {
                    if (backslash) {
                        currentToken.append(c);
                    } else {
                        switch (quote) {
                            case '\'' -> currentToken.append(c);
                            case '"' -> {
                                Character next = (i + 1 < charArray.length) ? charArray[i + 1] : null;
                                if (next != null && (next == '$' || next == '~' || next == '"' || next == '\\'
                                        || next == '\n')) {
                                    backslash = true;
                                    continue;
                                } else {
                                    currentToken.append(c);
                                }
                            }
                            case null -> {
                                backslash = true;
                                continue;
                            }
                            default -> {
                            }
                        }
                    }
                }
                default -> currentToken.append(c);
            }
            if (backslash) {
                backslash = false;
            }
        }
        if (!currentToken.isEmpty()) {
            result.add(currentToken.toString());
        }
        return result;
    }

    private static class ExtractResult {
        List<String> commands;
        Streams streams;

        ExtractResult(List<String> commands, Streams streams) {
            this.commands = commands;
            this.streams = streams;
        }
    }

    private static class Streams {
        File output;
        File err;
        boolean appendOutput;
        boolean appendErr;

        Streams(File output, File err, boolean appendOutput, boolean appendErr) {
            this.output = output;
            this.err = err;
            this.appendOutput = appendOutput;
            this.appendErr = appendErr;
        }
    }

    private ExtractResult extractStreams(List<String> parts) {
        var newCommands = new ArrayList<String>();
        File output = null;
        File err = null;
        String lastRedirection = null;
        boolean appendOutput = false;
        boolean appendErr = false;
        for (String command : parts) {
            if (lastRedirection != null) {
                switch (lastRedirection) {
                    case ">", "1>" -> {
                        output = new File(command);
                        appendOutput = false;
                        lastRedirection = null;
                    }
                    case "2>" -> {
                        err = new File(command);
                        appendErr = false;
                        lastRedirection = null;
                    }
                    case ">>", "1>>" -> {
                        output = new File(command);
                        appendOutput = true;
                        lastRedirection = null;
                    }
                    case "2>>" -> {
                        err = new File(command);
                        appendErr = true;
                        lastRedirection = null;
                    }
                }
            } else {
                switch (command) {
                    case ">", "1>", "2>", ">>", "1>>", "2>>" -> {
                        lastRedirection = command;
                    }
                    default -> {
                        newCommands.add(command);
                    }
                }
            }
        }
        return new ExtractResult(
                newCommands,
                new Streams(output, err, appendOutput, appendErr));
    }

    private Path handlePipeline(List<String> parts, Streams streams, Path currentDirectory) {
        int n = parts.size();
        if (n < 2) {
            System.err.println("Pipeline must have at least two commands.");
            return currentDirectory;
        }
        try {
            Command[] commands = new Command[n];
            boolean[] isBuiltin = new boolean[n];
            boolean allExternal = true;
            List<List<String>> tokenizedParts = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                List<String> tokens = Arrays.asList(parts.get(i).trim().split("\\s+"));
                tokenizedParts.add(tokens);
                String[] cmdArray = tokens.toArray(new String[0]);
                commands[i] = this.getCommand(cmdArray, "", null, null);
                if (commands[i] instanceof NoOpCommand) {
                    System.err.println(tokens.get(0) + ": command not found");
                    return currentDirectory;
                }
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
                    ProcessBuilder pb = new ProcessBuilder(tokenizedParts.get(i));
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
                                    tokenizedParts.get(idx).toArray(new String[0]),
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
                    ProcessBuilder pb = new ProcessBuilder(tokenizedParts.get(idx));
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

    public List<String> getHistory() {
        return history;
    }

    private void handleExternalCommand(List<String> commands, Streams streams, Path currentDirectory) {
        try {
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.directory(currentDirectory.toFile());
            if (streams.output != null) {
                if (streams.output.getParentFile() != null) {
                    streams.output.getParentFile().mkdirs();
                }
                if (streams.appendOutput) {
                    builder.redirectOutput(ProcessBuilder.Redirect.appendTo(streams.output));
                } else {
                    builder.redirectOutput(streams.output);
                }
            } else {
                builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            }
            if (streams.err != null) {
                if (streams.err.getParentFile() != null) {
                    streams.err.getParentFile().mkdirs();
                }
                if (streams.appendErr) {
                    builder.redirectError(ProcessBuilder.Redirect.appendTo(streams.err));
                } else {
                    builder.redirectError(streams.err);
                }
            } else {
                builder.redirectError(ProcessBuilder.Redirect.PIPE);
            }
            Process process = builder.start();
            if (streams.output == null || streams.err == null) {
                try (InputStream in = process.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        System.out.write(buffer, 0, len);
                    }
                }
                try (InputStream err = process.getErrorStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = err.read(buffer)) != -1) {
                        System.err.write(buffer, 0, len);
                    }
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println(commands.get(0) + ": command not found");
        }
    }
}