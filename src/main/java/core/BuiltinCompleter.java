package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws java.io.IOException {
        System.out.print(prompt);
        System.out.flush();
        StringBuilder inputBuffer = new StringBuilder();
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
                    String match = null;
                    for (String builtin : builtins) {
                        if (builtin.startsWith(current)) {
                            if (match == null) {
                                match = builtin;
                            } else {
                                match = null;
                                break;
                            }
                        }
                    }
                    if (match != null && !current.equals(match)) {
                        inputBuffer.setLength(0);
                        inputBuffer.append(match).append(' ');
                    }
                    clearLine(maxLineLength);
                    System.out.print(prompt + inputBuffer.toString());
                    System.out.flush();
                    maxLineLength = Math.max(maxLineLength, prompt.length() + inputBuffer.length());
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
        } catch (Exception e) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();
            if (line == null)
                return null;
            return line.trim();
        }
    }

    private static void clearLine(int totalLength) {
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}