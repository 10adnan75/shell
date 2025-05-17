package core;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public TokenizerResult tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false, inDouble = false, escape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escape) {
                if (inDouble || (!inSingle && !inDouble)) {
                    if (c == 'n')
                        current.append('\n');
                    else if (c == 't')
                        current.append('\t');
                    else
                        current.append(c);
                } else {
                    current.append('\\').append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            } else if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (Character.isWhitespace(c) && !inSingle && !inDouble) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0)
            tokens.add(current.toString());

        TokenizerResult result = new TokenizerResult(tokens);

        int redirectIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals(">") || token.equals("1>")) {
                redirectIndex = i;
                result.isRedirect = true;
                break;
            }
        }

        if (redirectIndex != -1 && redirectIndex + 1 < tokens.size()) {
            result.redirectTarget = tokens.get(redirectIndex + 1);
            tokens = tokens.subList(0, redirectIndex);
            result.tokens = tokens;
        }

        return result;
    }
}