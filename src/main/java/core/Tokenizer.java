package core;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public TokenizerResult tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean lastQuoted = false;
        int i = 0;

        String[] pipelineParts = input.split("\\|");
        if (pipelineParts.length > 1) {
            TokenizerResult result = new TokenizerResult(new ArrayList<>());
            result.isPipeline = true;
            result.pipelineParts = new ArrayList<>();

            for (String part : pipelineParts) {
                TokenizerResult partResult = tokenizePart(part.trim());
                result.pipelineParts.add(partResult);
            }

            return result;
        }

        return tokenizePart(input);
    }

    private TokenizerResult tokenizePart(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean lastQuoted = false;
        int i = 0;

        while (i < input.length()) {
            while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
                i++;
                lastQuoted = false;
            }
            if (i >= input.length())
                break;

            if (tokens.isEmpty()) {
                if (input.charAt(i) == '\'' || input.charAt(i) == '\"') {
                    char quote = input.charAt(i);
                    i++;
                    while (i < input.length() && input.charAt(i) != quote) {
                        if (quote == '\"' && input.charAt(i) == '\\' && i + 1 < input.length()) {
                            char next = input.charAt(i + 1);
                            if (next == '\\' || next == '$' || next == '\"' || next == '\n') {
                                sb.append(next);
                                i += 2;
                                continue;
                            } else {
                                sb.append('\\');
                                i++;
                                continue;
                            }
                        } else {
                            sb.append(input.charAt(i));
                            i++;
                        }
                    }
                    i++;
                    tokens.add(sb.toString());
                    sb.setLength(0);
                    lastQuoted = true;
                    continue;
                } else {
                    while (i < input.length() && !Character.isWhitespace(input.charAt(i))) {
                        if (input.charAt(i) == '\\') {
                            i++;
                            if (i < input.length()) {
                                sb.append(input.charAt(i));
                                i++;
                            } else {
                                sb.append('\\');
                            }
                        } else {
                            sb.append(input.charAt(i));
                            i++;
                        }
                    }
                    tokens.add(sb.toString());
                    sb.setLength(0);
                    lastQuoted = false;
                    continue;
                }
            }

            if (input.charAt(i) == '\'') {
                i++;
                while (i < input.length() && input.charAt(i) != '\'') {
                    sb.append(input.charAt(i));
                    i++;
                }
                i++;
                if (lastQuoted && !tokens.isEmpty()) {
                    int lastIndex = tokens.size() - 1;
                    tokens.set(lastIndex, tokens.get(lastIndex) + sb.toString());
                } else {
                    tokens.add(sb.toString());
                }
                sb.setLength(0);
                lastQuoted = true;
                continue;
            } else if (input.charAt(i) == '\"') {
                i++;
                while (i < input.length() && input.charAt(i) != '\"') {
                    if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '\\' || next == '$' || next == '\"' || next == '\n') {
                            sb.append(next);
                            i += 2;
                            continue;
                        } else {
                            sb.append('\\');
                            i++;
                            continue;
                        }
                    } else {
                        sb.append(input.charAt(i));
                        i++;
                    }
                }
                i++;
                if (lastQuoted && !tokens.isEmpty()) {
                    int lastIndex = tokens.size() - 1;
                    tokens.set(lastIndex, tokens.get(lastIndex) + sb.toString());
                } else {
                    tokens.add(sb.toString());
                }
                sb.setLength(0);
                lastQuoted = true;
                continue;
            }

            while (i < input.length() && !Character.isWhitespace(input.charAt(i))) {
                if (input.charAt(i) == '\\') {
                    i++;
                    if (i < input.length()) {
                        sb.append(input.charAt(i));
                        i++;
                    } else {
                        sb.append('\\');
                    }
                } else {
                    sb.append(input.charAt(i));
                    i++;
                }
            }
            if (lastQuoted && !tokens.isEmpty()) {
                int lastIndex = tokens.size() - 1;
                tokens.set(lastIndex, tokens.get(lastIndex) + sb.toString());
            } else {
                tokens.add(sb.toString());
            }
            sb.setLength(0);
            lastQuoted = true;
        }

        TokenizerResult result = new TokenizerResult(new ArrayList<>(tokens));

        for (int j = 0; j < result.tokens.size(); j++) {
            String token = result.tokens.get(j);
            if (token.equals(">") || token.equals("1>")) {
                if (j + 1 < result.tokens.size()) {
                    result.isRedirect = true;
                    result.redirectTarget = result.tokens.get(j + 1);
                    result.tokens = result.tokens.subList(0, j);
                    break;
                }
            } else if (token.equals(">>") || token.equals("1>>")) {
                if (j + 1 < result.tokens.size()) {
                    result.isRedirect = true;
                    result.isAppend = true;
                    result.redirectTarget = result.tokens.get(j + 1);
                    result.tokens = result.tokens.subList(0, j);
                    break;
                }
            } else if (token.equals("2>")) {
                if (j + 1 < result.tokens.size()) {
                    result.isStderrRedirect = true;
                    result.stderrRedirectTarget = result.tokens.get(j + 1);
                    result.tokens = result.tokens.subList(0, j);
                    break;
                }
            } else if (token.equals("2>>")) {
                if (j + 1 < result.tokens.size()) {
                    result.isStderrRedirect = true;
                    result.isStderrAppend = true;
                    result.stderrRedirectTarget = result.tokens.get(j + 1);
                    result.tokens = result.tokens.subList(0, j);
                    break;
                }
            }
        }

        return result;
    }
}