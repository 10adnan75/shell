package core;

import java.util.List;

public class TokenizerResult {
    public List<String> tokens;
    public String redirectTarget = null;
    public String stderrRedirectTarget = null;
    public boolean isRedirect = false;
    public boolean isStderrRedirect = false;
    public boolean isAppend = false;
    public boolean isStderrAppend = false;

    public TokenizerResult(List<String> tokens) {
        this.tokens = tokens;
    }
}