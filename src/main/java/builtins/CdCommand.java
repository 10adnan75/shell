package builtins;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CdCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2) {
            System.out.println("Usage: cd <directory>");
            return currentDirectory;
        }

        String target = args[1];
        Path newPath = Paths.get(target);

        if (!newPath.isAbsolute()) {
            newPath = currentDirectory.resolve(newPath).normalize();
        }

        File dir = newPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            return newPath;
        } else {
            System.out.println("cd: " + target + ": No such file or directory");
            return currentDirectory;
        }
    }
}