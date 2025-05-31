/**
 * TabCompleter.java
 *
 * <p>Provides tab completion for built-in and external commands.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.File;
import java.util.*;

/** Provides tab completion for built-in and external commands. */
public class TabCompleter {
  /** The array of built-in command names available for completion. */
  private final String[] builtins;

  /** The PATH environment variable used to locate external commands for completion. */
  private final String pathEnv;

  /**
   * Constructs a TabCompleter with the given builtins and PATH environment.
   *
   * @param builtins Array of builtin command names.
   * @param pathEnv The PATH environment variable.
   */
  public TabCompleter(String[] builtins, String pathEnv) {
    this.builtins = builtins;
    this.pathEnv = pathEnv;
  }

  /**
   * Returns the completed command or argument based on the current input.
   *
   * @param currentInput The current input string.
   * @return The completed input string.
   */
  public String complete(String currentInput) {
    String[] tokens = currentInput.trim().split("\\s+");
    if (tokens.length == 0 || currentInput.trim().isEmpty()) {
      System.out.print("\u0007");
      return currentInput;
    }
    String lastToken = tokens[tokens.length - 1];
    String[] paths = pathEnv != null ? pathEnv.split(":") : new String[0];
    Set<String> matchesSet = new LinkedHashSet<>();
    for (String cmd : builtins) {
      if (cmd.startsWith(lastToken)) {
        matchesSet.add(cmd);
      }
    }
    for (String path : paths) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        File[] files = dir.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.getName().startsWith(lastToken)) {
              matchesSet.add(file.getName());
            }
          }
        }
      }
    }
    List<String> matches = new ArrayList<>(matchesSet);
    Collections.sort(matches);
    if (matches.size() == 1) {
      String completion = matches.get(0);
      String prefix = "";
      int lastTokenIndex = currentInput.lastIndexOf(lastToken);
      if (lastTokenIndex > 0) {
        prefix = currentInput.substring(0, lastTokenIndex);
        if (!prefix.endsWith(" ")) prefix += " ";
      }
      return prefix + completion + " ";
    } else if (matches.size() > 1) {
      String lcp = longestCommonPrefix(matches);
      if (!lcp.equals(lastToken)) {
        String prefix = "";
        int lastTokenIndex = currentInput.lastIndexOf(lastToken);
        if (lastTokenIndex > 0) {
          prefix = currentInput.substring(0, lastTokenIndex);
          if (!prefix.endsWith(" ")) prefix += " ";
        }
        return prefix + lcp;
      } else {
        System.out.print("\u0007");
        System.out.println();
        for (String match : matches) {
          System.out.print(match + "  ");
        }
        System.out.println();
        System.out.print("$ " + currentInput);
      }
    } else {
      System.out.print("\u0007");
    }
    return currentInput;
  }

  /**
   * Returns the longest common prefix among a list of strings.
   *
   * @param strings The list of strings.
   * @return The longest common prefix.
   */
  private String longestCommonPrefix(List<String> strings) {
    if (strings == null || strings.isEmpty()) return "";
    String prefix = strings.get(0);
    for (int i = 1; i < strings.size(); i++) {
      while (!strings.get(i).startsWith(prefix)) {
        prefix = prefix.substring(0, prefix.length() - 1);
        if (prefix.isEmpty()) return "";
      }
    }
    return prefix;
  }
}
