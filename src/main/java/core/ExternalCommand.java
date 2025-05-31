/**
 * ExternalCommand.java
 *
 * <p>Represents and executes external (non-builtin) commands using ProcessBuilder. Handles output
 * and error redirection.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import builtins.Command;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents an external (non-builtin) command to be executed via ProcessBuilder.
 */
public class ExternalCommand implements Command {
  /** The list of arguments for the external command. */
  private final List<String> args;

  /** The file to which standard output should be redirected, or null if not used. */
  private final File redirectFile;

  /** The file to which standard error should be redirected, or null if not used. */
  private final File stderrRedirectFile;

  /** Enables debug output if set to true. */
  private static final boolean DEBUG = false;

  /**
   * Constructs an ExternalCommand with arguments and optional redirection files.
   *
   * @param args The command and its arguments.
   * @param redirectFile Output redirection file, if any.
   */
  public ExternalCommand(List<String> args, File redirectFile) {
    this.args = args;
    this.redirectFile = redirectFile;
    this.stderrRedirectFile = null;
  }

  /**
   * Constructs an ExternalCommand with arguments and optional redirection files for stdout and
   * stderr.
   *
   * @param args The command and its arguments.
   * @param redirectFile Output redirection file, if any.
   * @param stderrRedirectFile Error redirection file, if any.
   */
  public ExternalCommand(List<String> args, File redirectFile, File stderrRedirectFile) {
    this.args = args;
    this.redirectFile = redirectFile;
    this.stderrRedirectFile = stderrRedirectFile;
  }

  /**
   * Executes the external command and handles redirection.
   *
   * @param args The command and its arguments.
   * @param rawInput The raw input string.
   * @param currentDirectory The current working directory.
   * @return The current directory (unchanged).
   */
  @Override
  public Path execute(String[] args, String rawInput, Path currentDirectory) {
    try {
      ProcessBuilder pb = new ProcessBuilder(this.args);
      pb.directory(currentDirectory.toFile());
      pb.redirectErrorStream(false);

      if (redirectFile != null) {
        if (!redirectFile.getParentFile().exists()) {
          redirectFile.getParentFile().mkdirs();
        }
        if (!redirectFile.exists()) {
          redirectFile.createNewFile();
        }
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(redirectFile));
      }

      if (stderrRedirectFile != null) {
        try {
          if (!stderrRedirectFile.getParentFile().exists()) {
            stderrRedirectFile.getParentFile().mkdirs();
          }
          if (!stderrRedirectFile.exists()) {
            stderrRedirectFile.createNewFile();
          }
          pb.redirectError(ProcessBuilder.Redirect.appendTo(stderrRedirectFile));
        } catch (IOException e) {
          System.err.println("Error setting up stderr redirection: " + e.getMessage());
          return currentDirectory;
        }
      }

      if (DEBUG) {
        System.err.println("Executing command: " + String.join(" ", this.args));
      }

      Process process = pb.start();

      if (redirectFile == null) {
        try (var out = process.getInputStream()) {
          out.transferTo(System.out);
        }
      }

      if (stderrRedirectFile == null) {
        try (var err = process.getErrorStream()) {
          err.transferTo(System.err);
        }
      }

      int exitCode = process.waitFor();
      if (DEBUG) {
        System.err.println("Process exited with code: " + exitCode);
      }

      return currentDirectory;
    } catch (IOException e) {
      if (e.getMessage().contains("No such file or directory")
          || e.getMessage().contains("error=2")) {
        System.err.println(this.args.get(0) + ": command not found");
      } else {
        System.err.println("Error executing command: " + e.getMessage());
      }
      return currentDirectory;
    } catch (InterruptedException e) {
      System.err.println("Command execution interrupted: " + e.getMessage());
      Thread.currentThread().interrupt();
      return currentDirectory;
    } catch (Exception e) {
      System.err.println("Error executing command: " + e.getMessage());
      return currentDirectory;
    }
  }

  /**
   * Executes the external command with specified redirection files and append mode.
   *
   * @param redirectFile Output redirection file, if any.
   * @param stderrRedirectFile Error redirection file, if any.
   * @param isAppend Whether to append to the output files.
   * @throws IOException if an I/O error occurs.
   */
  public void execute(File redirectFile, File stderrRedirectFile, boolean isAppend)
      throws IOException {
    ProcessBuilder pb = new ProcessBuilder(this.args);
    pb.redirectErrorStream(false);

    if (redirectFile != null) {
      if (!redirectFile.getParentFile().exists()) {
        redirectFile.getParentFile().mkdirs();
      }
      if (!redirectFile.exists()) {
        redirectFile.createNewFile();
      }
      if (isAppend) {
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(redirectFile));
      } else {
        pb.redirectOutput(redirectFile);
      }
    }

    if (stderrRedirectFile != null) {
      try {
        if (!stderrRedirectFile.getParentFile().exists()) {
          stderrRedirectFile.getParentFile().mkdirs();
        }
        if (!stderrRedirectFile.exists()) {
          stderrRedirectFile.createNewFile();
        }
        if (isAppend) {
          pb.redirectError(ProcessBuilder.Redirect.appendTo(stderrRedirectFile));
        } else {
          pb.redirectError(stderrRedirectFile);
        }
      } catch (IOException e) {
        throw new IOException("Error setting up stderr redirection: " + e.getMessage());
      }
    }

    try {
      Process process = pb.start();
      process.waitFor();
    } catch (InterruptedException e) {
      throw new IOException("Command execution interrupted", e);
    }
  }

  /**
   * Returns the arguments for the external command.
   *
   * @return The list of arguments.
   */
  public List<String> getArgs() {
    return args;
  }
}
