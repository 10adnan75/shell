package builtins;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CdCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2) {
            System.err.println("Usage: cd <directory>");
            return currentDirectory;
        }

        String target = args[1];

        if (target.equals("~")) {
            target = System.getenv("HOME");
        }

        Path newPath = Paths.get(target);

        if (!newPath.isAbsolute()) {
            newPath = currentDirectory.resolve(newPath).normalize();
        }

        File dir = newPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            return newPath;
        } else {
            System.err.println("cd: " + args[1] + ": No such file or directory");
            return currentDirectory;
        }
    }
}