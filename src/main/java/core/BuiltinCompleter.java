package core;

import java.io.IOException;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws IOException {
        System.err.print(prompt);
        System.err.flush();
        StringBuilder inputBuffer = new StringBuilder();
        Trie trie = new Trie();
        for (String builtin : builtins) {
            trie.insert(builtin);
        }
        int maxLineLength = prompt.length();
        try (Termios _ = Termios.enableRawMode()) {
            System.err.print("\033[12l");
            System.err.flush();
            while (true) {
                int ch = System.in.read();
                if (ch == -1) {
                    System.err.println();
                    return null;
                }
                if (ch == '\n' || ch == '\r') {
                    System.err.println();
                    return inputBuffer.toString().trim();
                }
                if (ch == '\t') {
                    String current = inputBuffer.toString();
                    List<String> matches = trie.getCompletions(current);
                    if (matches.size() == 1) {
                        String match = matches.get(0);

                        if (!current.equals(match) && match.startsWith(current)) {
                            // Clear the current line
                            System.err.print("\033[2K\r");
                            System.err.flush();

                            String completion = match + " ";
                            inputBuffer.setLength(0);
                            inputBuffer.append(completion);

                            // Redraw the prompt and completion
                            System.err.print(prompt + completion);
                            System.err.flush();

                            maxLineLength = Math.max(maxLineLength, prompt.length() + completion.length());
                        }
                    }
                    continue;
                }
                if (ch == 127 || ch == 8) {
                    if (inputBuffer.length() > 0) {
                        inputBuffer.setLength(inputBuffer.length() - 1);
                        clearLine(maxLineLength);
                        System.err.print(prompt + inputBuffer.toString());
                        System.err.flush();
                    }
                    continue;
                }
                // Only echo printable characters to stderr (terminal), not stdout
                if (ch >= 32 && ch <= 126) {
                    inputBuffer.append((char) ch);
                    System.err.print((char) ch);
                    System.err.flush();
                    int lineLen = prompt.length() + inputBuffer.length();
                    maxLineLength = Math.max(maxLineLength, lineLen);
                }
            }
        }
    }

    private static void clearLine(int totalLength) {
        System.err.print("\033[2K\r");
        System.err.flush();
    }
}