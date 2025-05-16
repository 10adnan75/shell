import java.io.File;
import java.nio.file.Path;

import builtins.*;

public class CommandHandler {
    public static Command getCommand(String[] tokens, String rawInput) {
        String cmd = tokens[0];

        return switch (cmd) {
            case "exit" -> new ExitCommand();
            case "echo" -> new EchoCommand();
            case "type" -> new TypeCommand();
            case "pwd" -> new PwdCommand();
            case "cd" -> new CdCommand();
            default -> {
                if (isExecutableAvailable(cmd)) {
                    yield new ExternalCommand(tokens);
                } else {
                    System.out.println(rawInput + ": command not found");
                    yield new NoOpCommand();
                }
            }
        };
    }

    private static boolean isExecutableAvailable(String cmd) {
        for (String dir : System.getenv("PATH").split(":")) {
            File file = new File(dir, cmd);
            if (file.exists() && file.canExecute())
                return true;
        }
        return false;
    }

    public Path handleCommand(String[] inputSplit, String input, Path currentDirectory) {
        Command command = getCommand(inputSplit, input);
        return command.execute(inputSplit, input, currentDirectory);
    }
}