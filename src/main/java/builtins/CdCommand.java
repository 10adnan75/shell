package builtins;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CdCommand implements Command {

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {

        if (args.length < 2 || args[1].equals("~")) {
            String homePath = System.getenv("HOME");

            if (homePath != null) {
                return Paths.get(homePath);
            }
            return currentDirectory;
        }

        String pathStr = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Path targetDir = Paths.get(pathStr);

        if (!targetDir.isAbsolute()) {
            targetDir = currentDirectory.resolve(targetDir).normalize();
        }

        if (targetDir.toFile().exists() && targetDir.toFile().isDirectory()) {
            return targetDir;
        } else {
            System.out.println("cd: " + pathStr + ": No such file or directory");
            return currentDirectory;
        }
    }
}