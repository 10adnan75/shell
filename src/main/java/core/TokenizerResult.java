package core;

import java.util.List;

public class TokenizerResult {
    public List<String> tokens;
    public String redirectTarget = null;
    public boolean isRedirect = false;

    public TokenizerResult(List<String> tokens) {
        this.tokens = tokens;
    }
}