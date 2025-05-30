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

  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    return currentDirectory;
  }
}