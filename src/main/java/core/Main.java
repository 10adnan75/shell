/**
 * Main.java
 *
 * <p>Entry point for the Java Shell application. Initializes core components and starts the shell
 * loop.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the Java Shell application. Initializes core components and starts the shell
 * loop.
 */
public class Main {

  /** Constructs a new Main instance. Entry point class for the Java POSIX Shell application. */
  public Main() {}

  /**
   * The entry point of the Java POSIX Shell application. Initializes the command handler, built-in
   * commands, and shell input handler, then starts the interactive shell loop.
   *
   * @param args Command-line arguments (not used).
   * @throws Exception if an error occurs during shell initialization or execution.
   */
  public static void main(String[] args) throws Exception {
    CommandHandler handler = new CommandHandler();
    String[] builtins = {"echo", "exit", "pwd", "cd", "type", "history"};
    String pathEnv = System.getenv("PATH");
    Path currentDirectory = Paths.get("").toAbsolutePath();
    ShellInputHandler shell = new ShellInputHandler(handler, builtins, pathEnv);
    shell.run(currentDirectory);
  }
}
