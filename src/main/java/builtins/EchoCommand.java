/**
 * EchoCommand.java
 *
 * <p>Implements the 'echo' builtin command.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

/** Implements the 'echo' builtin command. */
public class EchoCommand implements Command {
  /**
   * Prints its arguments to standard output.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The current directory (unchanged).
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    if (args.length < 2) {
      return currentDirectory;
    }

    StringBuilder output = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      if (i > 1) {
        output.append(" ");
      }

      String arg = args[i];
      if ((arg.startsWith("\"") && arg.endsWith("\""))
          || (arg.startsWith("'") && arg.endsWith("'"))) {
        arg = arg.substring(1, arg.length() - 1);
      }

      output.append(arg);
    }

    System.out.println(output.toString());
    return currentDirectory;
  }
}
