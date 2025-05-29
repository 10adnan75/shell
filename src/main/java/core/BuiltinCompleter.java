package core;

import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins, List<String> history)
            throws java.io.IOException {
        System.err.print(prompt);
        System.err.flush();
        StringBuilder buffer = new StringBuilder();
        int maxLineLength = prompt.length();
        int historyIndex = history.size();
        try (core.Termios _ = core.Termios.enableRawMode()) {
            while (true) {
                int ch = System.in.read();
                if (ch == -1) {
                    System.err.println();
                    return null;
                }
                if (ch == '\n' || ch == '\r') {
                    // System.err.println();
                    return buffer.toString().trim();
                }
                if (ch == 27) {
                    int next1 = System.in.read();
                    int next2 = System.in.read();
                    if (next1 == 91 && next2 == 65) {
                        if (historyIndex > 0) {
                            historyIndex--;
                            buffer.setLength(0);
                            buffer.append(history.get(historyIndex));
                            clearLine(maxLineLength);
                            System.err.print(prompt + buffer.toString());
                            System.err.flush();
                            maxLineLength = Math.max(maxLineLength, prompt.length() + buffer.length());
                        }
                        continue;
                    }
                }
                if (ch == 127 || ch == 8) {
                    if (buffer.length() > 0) {
                        buffer.setLength(buffer.length() - 1);
                        clearLine(maxLineLength);
                        System.err.print(prompt + buffer.toString());
                        System.err.flush();
                    }
                    continue;
                }
                if (ch == '\t') {
                    continue;
                }
                if (ch >= 32 && ch <= 126) {
                    buffer.append((char) ch);
                    System.err.print((char) ch);
                    System.err.flush();
                    int lineLen = prompt.length() + buffer.length();
                    maxLineLength = Math.max(maxLineLength, lineLen);
                }
            }
        } catch (Exception e) {
            System.err.flush();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            String line = reader.readLine();
            if (line == null)
                return null;
            return line.trim();
        }
    }

    private static void clearLine(int totalLength) {
        System.err.print("\033[2K\r");
        System.err.flush();
    }
}