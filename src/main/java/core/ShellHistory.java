/**
 * ShellHistory.java
 *
 * <p>Manages the command history buffer and navigation (up/down arrows).
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.util.ArrayList;
import java.util.List;

/** Manages the command history buffer and navigation (up/down arrows). */
public class ShellHistory {
  private final List<String> history = new ArrayList<>();
  private int historyIndex = -1;

  /**
   * Adds a command to history and resets the navigation index.
   *
   * @param command The command to add.
   */
  public void add(String command) {
    history.add(command);
    historyIndex = -1;
  }

  /**
   * Returns the previous command in history.
   *
   * @return The previous command, or an empty string if none.
   */
  public String previous() {
    if (history.isEmpty()) return "";
    if (historyIndex == -1) historyIndex = history.size() - 1;
    else historyIndex = Math.max(0, historyIndex - 1);
    return history.get(historyIndex);
  }

  /**
   * Returns the next command in history.
   *
   * @return The next command, or an empty string if none.
   */
  public String next() {
    if (history.isEmpty() || historyIndex == -1) return "";
    historyIndex++;
    if (historyIndex >= history.size()) {
      historyIndex = -1;
      return "";
    }
    return history.get(historyIndex);
  }

  /** Resets the navigation index to the most recent command. */
  public void resetIndex() {
    historyIndex = -1;
  }
}
