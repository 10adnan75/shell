package builtins;

import java.nio.file.Path;

public class EchoCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length == 1) {
            System.out.println();
            return currentDirectory;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                output.append(" ");
            }
            output.append(args[i]);
        }
        System.out.println(output.toString());
        return currentDirectory;
    }
}