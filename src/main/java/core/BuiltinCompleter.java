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
                        // Only complete if current input is a prefix of the match
                        if (!current.equals(match) && match.startsWith(current)) {
                            // Use ANSI escape to clear entire line and move cursor to start
                            System.out.print("\033[2K\r");
                            System.out.flush();
                            
                            // Set the completion (command + space)
                            String completion = match + " ";
                            inputBuffer.setLength(0);
                            inputBuffer.append(completion);
                            
                            // Print the prompt and completion
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
                inputBuffer.append((char) ch);
                System.out.print((char) ch);
                System.out.flush();
                int lineLen = prompt.length() + inputBuffer.length();
                maxLineLength = Math.max(maxLineLength, lineLen);
            }
        }
        // return inputBuffer.toString();
    }

    private static void clearLine(int totalLength) {
        // Use ANSI escape sequence to clear the entire line
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}