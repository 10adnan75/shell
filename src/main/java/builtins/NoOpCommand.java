/**
 * NoOpCommand.java
 *
 * <p>Represents a no-operation command (used for error handling or unknown commands).
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

/** Represents a no-operation command (used for error handling or unknown commands). */
public class NoOpCommand implements Command {

  /**
   * Constructs a new NoOpCommand instance. Initializes the built-in no-operation command, which is
   * used as a fallback for unknown or invalid commands.
   */
  public NoOpCommand() {}

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
    return currentDirectory;
  }
}
