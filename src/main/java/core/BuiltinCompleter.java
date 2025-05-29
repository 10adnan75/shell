package core;

import java.io.IOException;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        StringBuilder inputBuffer = new StringBuilder();
        Trie trie = new Trie();
        for (String builtin : builtins) {
            trie.insert(builtin);
        }
        int maxLineLength = prompt.length();
        try (Termios _ = Termios.enableRawMode()) {
            System.out.print("\033[12l");
            System.out.flush();
            while (true) {
                int ch = System.in.read();
                if (ch == -1) {
                    System.out.println();
                    return null;
                }
                if (ch == '\n' || ch == '\r') {
                    System.out.println();
                    return inputBuffer.toString().trim();
                }
                if (ch == '\t') {
                    String current = inputBuffer.toString();
                    List<String> matches = trie.getCompletions(current);
                    if (matches.size() == 1) {
                        String match = matches.get(0);
                        if (!current.equals(match) && match.startsWith(current)) {
                            System.out.print("\033[2K\r");
                            System.out.flush();
                            String completion = match + " ";
                            inputBuffer.setLength(0);
                            inputBuffer.append(completion);
                            System.out.print(prompt + completion);
                            System.out.flush();
                            maxLineLength = Math.max(maxLineLength, prompt.length() + completion.length());
                        }
                    }
                    continue;
                }
                if (ch == 127 || ch == 8) {
                    if (inputBuffer.length() > 0) {
                        inputBuffer.setLength(inputBuffer.length() - 1);
                        clearLine(maxLineLength);
                        System.out.print(prompt + inputBuffer.toString());
                        System.out.flush();
                    }
                    continue;
                }
                if (ch >= 32 && ch <= 126) {
                    inputBuffer.append((char) ch);
                    System.out.print((char) ch);
                    System.out.flush();
                    int lineLen = prompt.length() + inputBuffer.length();
                    maxLineLength = Math.max(maxLineLength, lineLen);
                }
            }
        }
    }

    private static void clearLine(int totalLength) {
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}