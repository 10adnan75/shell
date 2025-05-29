/**
 * TestOutputCapture.java
 *
 * <p>Utility for capturing System.out and System.err output during tests.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Utility for capturing System.out and System.err output during tests. */
public class TestOutputCapture implements AutoCloseable {
  private final PrintStream originalOut;
  private final PrintStream originalErr;
  private final ByteArrayOutputStream outContent;
  private final ByteArrayOutputStream errContent;

  /** Starts capturing System.out and System.err. */
  public TestOutputCapture() {
    this.originalOut = System.out;
    this.originalErr = System.err;
    this.outContent = new ByteArrayOutputStream();
    this.errContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Returns the captured System.out output as a string.
   *
   * @return Captured stdout.
   */
  public String getStdOut() {
    return outContent.toString();
  }

  /**
   * Returns the captured System.err output as a string.
   *
   * @return Captured stderr.
   */
  public String getStdErr() {
    return errContent.toString();
  }

  /** Restores System.out and System.err to their original streams. */
  @Override
  public void close() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }
}
