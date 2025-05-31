/**
 * ShellInputHandler.java
 *
 * <p>Handles the main shell loop, user input, prompt display, command history navigation, and tab
 * completion.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * Handles the main shell loop, user input, prompt display, command history navigation, and tab
 * completion.
 */
public class ShellInputHandler {
  /** The command handler responsible for executing commands. */
  private final CommandHandler handler;

  /** The shell history manager for storing and navigating command history. */
  private final ShellHistory history;

  /** The tab completer for providing command and file completions. */
  private final TabCompleter tabCompleter;

  /**
   * Constructs a ShellInputHandler with the given command handler, builtins, and PATH environment.
   *
   * @param handler The CommandHandler instance.
   * @param builtins Array of builtin command names.
   * @param pathEnv The PATH environment variable.
   */
  public ShellInputHandler(CommandHandler handler, String[] builtins, String pathEnv) {
    this.handler = handler;
    this.history = new ShellHistory();
    this.tabCompleter = new TabCompleter(builtins, pathEnv);
  }

  /**
   * Runs the main shell loop, reading user input, handling special keys, and delegating command
   * execution.
   *
   * @param currentDirectory The initial working directory.
   * @throws Exception if an error occurs during shell operation.
   */
  public void run(Path currentDirectory) throws Exception {
    boolean running = true;
    try {
      ProcessBuilder processBuilder =
          new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
      processBuilder.directory(new File("").getCanonicalFile());
      Process rawMode = processBuilder.start();
      rawMode.waitFor();

      final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      while (running) {
        System.out.flush();
        System.err.flush();
        String tempString = "";
        if (isInteractive()) {
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
              currentDirectory = handler.handleCommand(tempString.trim(), currentDirectory);
            }
            if (!isInteractive()) {
              System.out.print("\n");
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
        ProcessBuilder resetBuilder =
            new ProcessBuilder("/bin/sh", "-c", "stty echo icanon < /dev/tty");
        resetBuilder.directory(new File("").getCanonicalFile());
        Process resetProcess = resetBuilder.start();
        resetProcess.waitFor();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Determines if the shell is running in interactive mode (vs. test/script mode).
   *
   * @return true if interactive, false otherwise.
   */
  private boolean isInteractive() {
    String[] testVars = {"CODECRAFTERS_TEST", "CI", "TEST"};
    for (String var : testVars) {
      if (System.getenv(var) != null) {
        return false;
      }
    }
    if (System.getenv("TERM") == null) {
      return false;
    }
    return System.console() != null;
  }
}
