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
                if (inDouble) {
                    switch (c) {
                        case 'n':
                            current.append('\n');
                            break;
                        case 't':
                            current.append('\t');
                            break;
                        case '"':
                            current.append('"');
                            break;
                        case '\\':
                            current.append('\\');
                            break;
                        case '\'':
                            current.append('\'');
                            break;
                        default:
                            current.append('\\').append(c);
                            break;
                    }
                } else if (inSingle) {
                    current.append('\\').append(c);
                } else {
                    switch (c) {
                        case 'n':
                            current.append('\n');
                            break;
                        case 't':
                            current.append('\t');
                            break;
                        case '"':
                            current.append('"');
                            break;
                        case '\\':
                            current.append('\\');
                            break;
                        case '\'':
                            current.append('\'');
                            break;
                        default:
                            current.append('\\').append(c);
                            break;
                    }
                }
                escape = false;
            } else if (c == '\\' && !inSingle) {
                escape = true;
            } else if (c == '\\' && inSingle) {
                current.append('\\');
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

        TokenizerResult result = new TokenizerResult(new ArrayList<>(tokens));

        int redirectIndex = -1;
        for (int i = 0; i < result.tokens.size(); i++) {
            String token = result.tokens.get(i);
            if (token.equals(">") || token.equals("1>")) {
                redirectIndex = i;
                result.isRedirect = true;
                break;
            }
        }

        if (redirectIndex != -1 && redirectIndex + 1 < result.tokens.size()) {
            result.redirectTarget = result.tokens.get(redirectIndex + 1);
            result.tokens = result.tokens.subList(0, redirectIndex);
        }

        return result;
    }
}