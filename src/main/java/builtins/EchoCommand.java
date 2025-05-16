package builtins;

import java.nio.file.Path;

public class EchoCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (rawInput.length() > 5) {
            System.out.println(rawInput.substring(5));
        } else {
            System.out.println();
        }
        return currentDirectory;
    }
}