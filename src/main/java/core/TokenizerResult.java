/**
 * TokenizerResult.java
 *
 * <p>Holds the result of tokenizing a shell input string, including tokens and pipeline parts.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.util.List;

/** Holds the result of tokenizing a shell input string, including tokens and pipeline parts. */
public class TokenizerResult {
  public List<String> tokens;
  public String redirectTarget = null;
  public String stderrRedirectTarget = null;
  public boolean isRedirect = false;
  public boolean isStderrRedirect = false;
  public boolean isAppend = false;
  public boolean isStderrAppend = false;
  public boolean isPipeline = false;
  public List<TokenizerResult> pipelineParts = null;

  /**
   * Constructs a TokenizerResult with the given tokens.
   *
   * @param tokens The list of tokens.
   */
  public TokenizerResult(List<String> tokens) {
    this.tokens = tokens;
  }
}
