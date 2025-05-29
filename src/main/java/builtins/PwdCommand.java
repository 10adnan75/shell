package builtins;

import java.nio.file.Path;

public class PwdCommand implements Command {
    
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        System.out.println(currentDirectory.toAbsolutePath());
        return currentDirectory;
    }
}