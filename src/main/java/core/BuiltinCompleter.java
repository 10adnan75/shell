package core;

import java.io.IOException;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws java.io.IOException {
        System.out.print(prompt);
        System.out.flush();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null)
            return null;
        return line.trim();
    }

    private static void clearLine(int totalLength) {
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}