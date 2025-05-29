package builtins;

import java.nio.file.Path;

public class NoOpCommand implements Command {
    
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        return currentDirectory;
    }
}