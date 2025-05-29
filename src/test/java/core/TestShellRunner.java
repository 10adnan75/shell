/**
 * TestShellRunner.java
 *
 * <p>Utility for running shell commands and capturing their output in tests.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Utility for running shell commands and capturing their output in tests. */
public class TestShellRunner {
  /**
   * Runs a shell command and returns its output as a string.
   *
   * @param command The command to run.
   * @return The output of the command (stdout and stderr).
   * @throws IOException if an I/O error occurs.
   * @throws InterruptedException if the process is interrupted.
   */
  public static String runCommand(String command) throws IOException, InterruptedException {
    Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader =
            new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append(System.lineSeparator());
      }
      while ((line = errReader.readLine()) != null) {
        output.append(line).append(System.lineSeparator());
      }
    }
    process.waitFor();
    return output.toString();
  }
}
