/**
 * EchoCommand.java
 *
 * Implements the 'echo' builtin command.
 *
 * Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;

public class EchoCommand implements Command {

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2) {
            return currentDirectory;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                output.append(" ");
            }

            String arg = args[i];
            if ((arg.startsWith("\"") && arg.endsWith("\"")) ||
                    (arg.startsWith("'") && arg.endsWith("'"))) {
                arg = arg.substring(1, arg.length() - 1);
            }

            output.append(arg);
        }

        System.out.println(output.toString());
        return currentDirectory;
    }
}