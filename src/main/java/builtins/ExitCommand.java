package builtins;

import java.nio.file.Path;

public class ExitCommand implements Command {

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        System.exit(0);
        return currentDirectory;
    }
}