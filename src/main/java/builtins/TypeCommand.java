package builtins;

import java.io.File;
import java.nio.file.Path;

public class TypeCommand implements Command {
    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        if (args.length < 2) {
            System.out.println("Usage: type <command>");
            return currentDirectory;
        }

        String cmd = args[1];
        if (cmd.equals("echo") || cmd.equals("type") || cmd.equals("exit") || cmd.equals("pwd") || cmd.equals("cd")) {
            System.out.println(cmd + " is a shell builtin");
        } else {
            boolean found = false;
            for (String dir : System.getenv("PATH").split(":")) {
                File file = new File(dir, cmd);
                if (file.exists() && file.canExecute()) {
                    System.out.println(cmd + " is " + file.getAbsolutePath());
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println(cmd + ": not found");
            }
        }

        return currentDirectory;
    }
}