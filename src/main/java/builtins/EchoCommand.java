package builtins;

import java.nio.file.Path;

public class EchoCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length > 1) {
            StringBuilder output = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                output.append(args[i]);
                if (i < args.length - 1) {
                    output.append(" ");
                }
            }
            System.out.print(output.toString());
        } else {
            System.out.print("");
        }
        return currentDirectory;
    }
}