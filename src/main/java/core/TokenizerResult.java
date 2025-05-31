/**
 * TokenizerResult.java
 *
 * <p>Holds the result of tokenizing a shell input string, including tokens and pipeline parts.
 *
 * <p>Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.util.List;

/**
 * Holds the result of tokenizing a shell input string, including tokens and pipeline parts.
 */
public class TokenizerResult {
  /** The list of tokens parsed from the input string. */
  public List<String> tokens;

  /** The target file for standard output redirection (e.g., after '>' or '>>'), or null if not present. */
  public String redirectTarget = null;

  /** The target file for standard error redirection (e.g., after '2>' or '2>>'), or null if not present. */
  public String stderrRedirectTarget = null;

  /** True if the input contains standard output redirection ('>' or '>>'). */
  public boolean isRedirect = false;

  /** True if the input contains standard error redirection ('2>' or '2>>'). */
  public boolean isStderrRedirect = false;

  /** True if the output redirection is in append mode ('>>'). */
  public boolean isAppend = false;

  /** True if the error redirection is in append mode ('2>>'). */
  public boolean isStderrAppend = false;

  /** True if the input contains a pipeline ('|'). */
  public boolean isPipeline = false;

  /** The list of TokenizerResult objects for each part of a pipeline, or null if not a pipeline. */
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
