/**
 * CdCommand.java
 *
 * <p>Implements the 'cd' (change directory) builtin command.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/** Implements the 'cd' (change directory) builtin command. */
public class CdCommand implements Command {
  /**
   * Changes the shell's current directory and returns the new path.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The new current directory after execution.
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    if (args.length < 2 || args[1].equals("~")) {
      String homePath = System.getenv("HOME");
      if (homePath != null) {
        Path homeDirPath = Paths.get(homePath);
        if (Files.exists(homeDirPath) && Files.isDirectory(homeDirPath)) {
          return homeDirPath;
        }
      }
      return currentDirectory;
    }

    String pathStr = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    Path targetDir = Paths.get(pathStr);

    if (!targetDir.isAbsolute()) {
      targetDir = currentDirectory.resolve(targetDir).normalize();
    } else {
      targetDir = targetDir.normalize();
    }

    if (Files.exists(targetDir)) {
      if (Files.isDirectory(targetDir)) {
        try {
          Files.isReadable(targetDir);
          return targetDir;
        } catch (Exception e) {
          System.err.println("cd: " + pathStr + ": Permission denied");
          return currentDirectory;
        }
      } else {
        System.err.println("cd: " + pathStr + ": Not a directory");
        return currentDirectory;
      }
    } else {
      System.err.println("cd: " + pathStr + ": No such file or directory");
      return currentDirectory;
    }
  }
}
