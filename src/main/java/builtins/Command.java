/**
 * Command.java
 *
 * <p>Interface for all shell commands (builtins and external).
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

/** Interface for all shell commands (builtins and external). */
public interface Command {
  /**
   * Executes the command and returns the (possibly updated) current directory.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The updated current directory after execution.
   */
  Path execute(String[] args, String rawInput, Path currentDirectory);
}
