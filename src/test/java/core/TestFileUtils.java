/**
 * TestFileUtils.java
 *
 * <p>Utility methods for creating, deleting, and managing temporary files and directories during
 * tests.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods for creating, deleting, and managing temporary files and directories during
 * tests.
 */
public class TestFileUtils {
  /**
   * Creates a temporary file with the given prefix and suffix.
   *
   * @param prefix File name prefix.
   * @param suffix File name suffix.
   * @return Path to the created temporary file.
   * @throws IOException if an I/O error occurs.
   */
  public static Path createTempFile(String prefix, String suffix) throws IOException {
    return Files.createTempFile(prefix, suffix);
  }

  /**
   * Creates a temporary directory with the given prefix.
   *
   * @param prefix Directory name prefix.
   * @return Path to the created temporary directory.
   * @throws IOException if an I/O error occurs.
   */
  public static Path createTempDirectory(String prefix) throws IOException {
    return Files.createTempDirectory(prefix);
  }

  /**
   * Deletes a file or directory recursively.
   *
   * @param path Path to delete.
   * @throws IOException if an I/O error occurs.
   */
  public static void deleteRecursively(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      try (var entries = Files.list(path)) {
        for (Path entry : entries.toList()) {
          deleteRecursively(entry);
        }
      }
    }
    Files.deleteIfExists(path);
  }
}
