package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins, List<String> history)
            throws java.io.IOException {
        System.out.print(prompt);
        System.out.flush();
        StringBuilder buffer = new StringBuilder();
        int maxLineLength = prompt.length();
        int historyIndex = history.size();
        try (core.Termios _ = core.Termios.enableRawMode()) {
            while (true) {
                int ch = System.in.read();
                if (ch == -1) {
                    System.out.println();
                    return null;
                }
                if (ch == '\n' || ch == '\r') {
                    System.out.println();
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
                            System.out.print(prompt + buffer.toString());
                            System.out.flush();
                            maxLineLength = Math.max(maxLineLength, prompt.length() + buffer.length());
                        }
                        continue;
                    }
                }
                if (ch == 127 || ch == 8) {
                    if (buffer.length() > 0) {
                        buffer.setLength(buffer.length() - 1);
                        clearLine(maxLineLength);
                        System.out.print(prompt + buffer.toString());
                        System.out.flush();
                    }
                    continue;
                }
                if (ch == '\t') {
                    continue;
                }
                if (ch >= 32 && ch <= 126) {
                    buffer.append((char) ch);
                    System.out.print((char) ch);
                    System.out.flush();
                    int lineLen = prompt.length() + buffer.length();
                    maxLineLength = Math.max(maxLineLength, lineLen);
                }
            }
        } catch (Exception e) {
            System.out.flush();
            StringBuilder fallbackBuffer = new StringBuilder();
            int c;
            while ((c = System.in.read()) != -1) {
                if (c == '\n' || c == '\r') {
                    System.out.println();
                    break;
                }
                fallbackBuffer.append((char) c);
                System.out.print((char) c);
                System.out.flush();
            }
            if (c == -1 && fallbackBuffer.length() == 0)
                return null;
            return fallbackBuffer.toString().trim();
        }
    }

    private static void clearLine(int totalLength) {
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}