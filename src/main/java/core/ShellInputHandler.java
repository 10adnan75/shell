package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ShellInputHandler {
    private final CommandHandler handler;
    private final ShellHistory history;
    private final TabCompleter tabCompleter;

    public ShellInputHandler(CommandHandler handler, String[] builtins, String pathEnv) {
        this.handler = handler;
        this.history = new ShellHistory();
        this.tabCompleter = new TabCompleter(builtins, pathEnv);
    }

    public void run(Path currentDirectory) throws Exception {
        boolean running = true;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            processBuilder.directory(new File("").getCanonicalFile());
            Process rawMode = processBuilder.start();
            rawMode.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (running) {
                System.out.flush();
                System.err.flush();
                String tempString = "";
                if (System.console() != null) {
                    System.out.print("$ ");
                }
                history.resetIndex();

                while (true) {
                    int readVal = reader.read();
                    if (readVal == -1) {
                        running = false;
                        break;
                    }
                    char c = (char) readVal;
                    if (c == 27) {
                        char next = (char) reader.read();
                        if (next == '[') {
                            char arrow = (char) reader.read();
                            if (arrow == 'A') {
                                System.out.print("\r$ " + " ".repeat(tempString.length()) + "\r$ ");
                                tempString = history.previous();
                                System.out.print(tempString);
                                continue;
                            } else if (arrow == 'B') {
                                System.out.print("\r$ " + " ".repeat(tempString.length()) + "\r$ ");
                                tempString = history.next();
                                System.out.print(tempString);
                                continue;
                            }
                        }
                    }
                    if (c == '\n' || c == '\r') {
                        System.out.print('\n');
                        if (!tempString.trim().isEmpty()) {
                            history.add(tempString.trim());
                            handler.handleCommand(tempString.trim(), currentDirectory);
                        }
                        break;
                    } else if (c == '\t') {
                        tempString = tabCompleter.complete(tempString);
                        System.out.print("\r$ " + tempString);
                    } else if (c == '\b' || c == 127) {
                        if (tempString.length() > 0) {
                            System.out.print("\b \b");
                            tempString = tempString.substring(0, tempString.length() - 1);
                        }
                    } else if (c >= 32 && c <= 126) {
                        System.out.print(c);
                        tempString += c;
                    } else if (c == 3) {
                        System.out.println();
                        break;
                    } else if (c == 4) {
                        running = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing terminal: " + e.getMessage());
        } finally {
            try {
                ProcessBuilder resetBuilder = new ProcessBuilder(
                        "/bin/sh", "-c", "stty echo icanon < /dev/tty");
                resetBuilder.directory(new File("").getCanonicalFile());
                Process resetProcess = resetBuilder.start();
                resetProcess.waitFor();
            } catch (Exception e) {
            }
        }
    }
}