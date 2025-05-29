package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins, List<String> history) throws java.io.IOException {
        System.out.print(prompt);
        System.out.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null)
            return null;
        return line.trim();
    }
}