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

public class Main {
  public static void main(String[] args) throws Exception {
    CommandHandler handler = new CommandHandler();
    String[] builtins = {"echo", "exit", "pwd", "cd", "type", "history"};
    String pathEnv = System.getenv("PATH");
    Path currentDirectory = Paths.get("").toAbsolutePath();
    ShellInputHandler shell = new ShellInputHandler(handler, builtins, pathEnv);
    shell.run(currentDirectory);
  }
}
