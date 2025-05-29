/**
 * CommandHandlerTest.java
 *
 * <p>Unit tests for the CommandHandler class, covering built-in command behavior.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Unit tests for the CommandHandler class, covering built-in command behavior. */
public class CommandHandlerTest {
  /** Tests that the echo command executes without error and does not change the directory. */
  @Test
  void testEchoCommand() {
    CommandHandler handler = new CommandHandler();
    Path initialDir = Path.of(System.getProperty("user.dir"));
    Path result = handler.handleCommand("echo Hello, World!", initialDir);
    assertEquals(initialDir, result);
  }

  /** Tests that the cd command changes the directory to /tmp. */
  @Test
  void testCdCommand() {
    CommandHandler handler = new CommandHandler();
    Path initialDir = Path.of(System.getProperty("user.dir"));
    Path tmpDir = Path.of("/tmp");
    Path result = handler.handleCommand("cd /tmp", initialDir);
    assertEquals(tmpDir, result);
  }
}
