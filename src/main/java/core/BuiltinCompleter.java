package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        StringBuilder inputBuffer = new StringBuilder();
        boolean justCompleted = false;
        try (var scope = Termios.enableRawMode()) {
            while (true) {
                int ch = System.in.read();
                if (ch == -1) {
                    System.out.println();
                    return null;
                }
                if (ch == '\n' || ch == '\r') {
                    System.out.println();
                    break;
                }
                if (ch == '\t') {
                    String current = inputBuffer.toString();
                    List<String> matches = new ArrayList<>();
                    for (String builtin : builtins) {
                        if (builtin.startsWith(current)) {
                            matches.add(builtin);
                        }
                    }
                    if (matches.size() == 1) {
                        String match = matches.get(0);
                        if (!current.equals(match)) {
                            System.out.print("\r" + " ".repeat(prompt.length() + inputBuffer.length()) + "\r");
                            System.out.print(prompt + match + " ");
                            System.out.flush();
                            inputBuffer.setLength(0);
                            inputBuffer.append(match).append(' ');
                            try {
                                while (System.in.available() > 0) {
                                    System.in.read();
                                }
                            } catch (Exception ignore) {
                            }
                            justCompleted = true;
                        }
                        continue;
                    }
                    System.out.flush();
                    continue;
                }
                if (justCompleted) {
                    justCompleted = false;
                    continue;
                }
                if (ch == 127 || ch == 8) {
                    if (inputBuffer.length() > 0) {
                        inputBuffer.setLength(inputBuffer.length() - 1);
                        System.out.print("\b \b");
                        System.out.flush();
                    }
                    continue;
                }
                inputBuffer.append((char) ch);
                System.out.print((char) ch);
                System.out.flush();
            }
        }
        return inputBuffer.toString();
    }
}