package builtins;

import java.nio.file.Path;
import java.util.Arrays;

public class EchoCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length > 1) {
            System.out.println(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        } else {
            System.out.println();
        }
        return currentDirectory;
    }
}