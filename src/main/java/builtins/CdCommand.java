/**
 * CdCommand.java
 *
 * Implements the 'cd' (change directory) builtin command.
 *
 * Author: Adnan Mazharuddin Shaikh
 */
package builtins;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

public class CdCommand implements Command {

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2 || args[1].equals("~")) {
            String homePath = System.getenv("HOME");
            if (homePath != null) {
                Path homeDirPath = Paths.get(homePath);
                if (Files.exists(homeDirPath) && Files.isDirectory(homeDirPath)) {
                    return homeDirPath;
                }
            }
            return currentDirectory;
        }

        String pathStr = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Path targetDir = Paths.get(pathStr);

        if (!targetDir.isAbsolute()) {
            targetDir = currentDirectory.resolve(targetDir).normalize();
        } else {
            targetDir = targetDir.normalize();
        }

        if (Files.exists(targetDir)) {
            if (Files.isDirectory(targetDir)) {
                try {
                    Files.isReadable(targetDir);
                    return targetDir;
                } catch (Exception e) {
                    System.err.println("cd: " + pathStr + ": Permission denied");
                    return currentDirectory;
                }
            } else {
                System.err.println("cd: " + pathStr + ": Not a directory");
                return currentDirectory;
            }
        } else {
            System.err.println("cd: " + pathStr + ": No such file or directory");
            return currentDirectory;
        }
    }
}