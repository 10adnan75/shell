/**
 * TabCompleter.java
 *
 * Provides tab completion for built-in and external commands.
 *
 * Author: Adnan Mazharuddin Shaikh
 */
package core;

import java.io.File;
import java.util.*;

public class TabCompleter {
    private final String[] builtins;
    private final String pathEnv;

    public TabCompleter(String[] builtins, String pathEnv) {
        this.builtins = builtins;
        this.pathEnv = pathEnv;
    }

    public String complete(String currentInput) {
        String[] tokens = currentInput.trim().split("\\s+");
        if (tokens.length == 0 || currentInput.trim().isEmpty()) {
            System.out.print("\u0007");
            return currentInput;
        }
        String lastToken = tokens[tokens.length - 1];
        String[] paths = pathEnv != null ? pathEnv.split(":") : new String[0];
        Set<String> matchesSet = new LinkedHashSet<>();
        for (String cmd : builtins) {
            if (cmd.startsWith(lastToken)) {
                matchesSet.add(cmd);
            }
        }
        for (String path : paths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith(lastToken)) {
                            matchesSet.add(file.getName());
                        }
                    }
                }
            }
        }
        List<String> matches = new ArrayList<>(matchesSet);
        Collections.sort(matches);
        if (matches.size() == 1) {
            String completion = matches.get(0);
            String prefix = "";
            int lastTokenIndex = currentInput.lastIndexOf(lastToken);
            if (lastTokenIndex > 0) {
                prefix = currentInput.substring(0, lastTokenIndex);
                if (!prefix.endsWith(" "))
                    prefix += " ";
            }
            return prefix + completion + " ";
        } else if (matches.size() > 1) {
            String lcp = longestCommonPrefix(matches);
            if (!lcp.equals(lastToken)) {
                String prefix = "";
                int lastTokenIndex = currentInput.lastIndexOf(lastToken);
                if (lastTokenIndex > 0) {
                    prefix = currentInput.substring(0, lastTokenIndex);
                    if (!prefix.endsWith(" "))
                        prefix += " ";
                }
                return prefix + lcp;
            } else {
                System.out.print("\u0007");
                System.out.println();
                for (String match : matches) {
                    System.out.print(match + "  ");
                }
                System.out.println();
                System.out.print("$ " + currentInput);
            }
        } else {
            System.out.print("\u0007");
        }
        return currentInput;
    }

    private String longestCommonPrefix(List<String> strings) {
        if (strings == null || strings.isEmpty())
            return "";
        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            while (!strings.get(i).startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty())
                    return "";
            }
        }
        return prefix;
    }
}