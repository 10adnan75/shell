/**
 * TypeCommand.java
 *
 * Implements the 'type' builtin command, which tells whether a command is builtin or external.
 *
 * Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TypeCommand implements Command {

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2) {
            System.out.println("Usage: type [command]");
            return currentDirectory;
        }

        String targetCommand = args[1];
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd", "cd", "history");

        if (builtins.contains(targetCommand)) {
            System.out.println(targetCommand + " is a shell builtin");
        } else {
            String pathEnv = System.getenv("PATH");

            if (pathEnv != null) {
                for (String dir : pathEnv.split(":")) {
                    File file = new File(dir, targetCommand);

                    if (file.exists() && file.canExecute()) {
                        System.out.println(targetCommand + " is " + file.getAbsolutePath());
                        return currentDirectory;
                    }
                }
            }

            System.out.println(targetCommand + ": not found");
        }

        return currentDirectory;
    }
}