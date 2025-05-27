package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        StringBuilder inputBuffer = new StringBuilder();
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
                    String completion = matches.get(0).substring(current.length());
                    System.out.print(completion + " ");
                    inputBuffer.append(completion).append(' ');
                }
                System.out.flush();
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
        return inputBuffer.toString();
    }
}