import java.nio.file.Path;
import builtins.Command;

public class NoOpCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        return currentDirectory;
    }
}