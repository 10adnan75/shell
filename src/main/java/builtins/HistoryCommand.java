/**
 * HistoryCommand.java
 *
 * <p>Implements the 'history' builtin command.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;
import java.util.List;

/** Implements the 'history' builtin command. */
public class HistoryCommand implements Command {
  private final List<String> history;

  /**
   * Constructs a HistoryCommand with the given history list.
   *
   * @param history The command history list.
   */
  public HistoryCommand(List<String> history) {
    this.history = history;
  }

  /**
   * Prints the command history.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The current directory (unchanged).
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    int limit = history.size();

    if (args.length > 1) {
      try {
        limit = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("history: numeric argument required");
        return currentDirectory;
      }
    }

    int start = Math.max(0, history.size() - limit);
    for (int i = start; i < history.size(); i++) {
      System.out.printf("%5d  %s%n", i + 1, history.get(i));
    }

    return currentDirectory;
  }
}
