package builtins;

import java.nio.file.Path;

public interface Command {
    Path execute(String[] args, String rawInput, Path currentDirectory);
}