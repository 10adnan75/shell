/**
 * PwdCommand.java
 *
 * <p>Implements the 'pwd' (print working directory) builtin command.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

/** Implements the 'pwd' (print working directory) builtin command. */
public class PwdCommand implements Command {
  /**
   * Prints the current directory.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The current directory (unchanged).
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    System.out.println(currentDirectory.toAbsolutePath());
    return currentDirectory;
  }
}
