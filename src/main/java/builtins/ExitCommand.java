/**
 * ExitCommand.java
 *
 * <p>Implements the 'exit' builtin command.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

/** Implements the 'exit' builtin command. */
public class ExitCommand implements Command {

  /** Constructs a new ExitCommand instance. Initializes the built-in 'exit' command. */
  public ExitCommand() {}

  /**
   * Exits the shell with the given status code.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The current directory (unreachable).
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    System.exit(0);
    return currentDirectory;
  }
}
