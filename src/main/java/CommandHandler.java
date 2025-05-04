import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

class CommandHandler {
    private final String[] paths;

    public CommandHandler() {
        this.paths = System.getenv("PATH").split(":");
    }

    public void handle(String input) {
        String[] tokens = input.split(" ");
        String command = tokens[0];

        switch (command) {
            case "exit" -> System.exit(0);
            case "echo" -> handleEcho(input);
            case "type" -> handleType(tokens);
            default -> handleExternalCommand(tokens, input);
        }
    }

    private void handleEcho(String input) {
        if (input.length() > 5) {
            System.out.println(input.substring(5));
        } else {
            System.out.println();
        }
    }

    private void handleType(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: type <command>");
            return;
        }

        String cmd = tokens[1];
        if (cmd.equals("echo") || cmd.equals("type") || cmd.equals("exit") || cmd.equals("pwd")) {
            System.out.println(cmd + " is a shell builtin");
            return;
        }

        for (String dir : paths) {
            File file = new File(dir, cmd);
            if (file.exists() && file.canExecute()) {
                System.out.println(cmd + " is " + file.getAbsolutePath());
                return;
            }
        }

        System.out.println(cmd + ": not found");
    }

    private void handleExternalCommand(String[] tokens, String originalInput) {
        File executable = null;

        for (String dir : paths) {
            File file = new File(dir, tokens[0]);
            if (file.exists() && file.canExecute()) {
                executable = file;
                break;
            }
        }

        if (executable != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder(tokens);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                process.waitFor();
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        } else {
            System.out.println(originalInput + ": command not found");
        }
    }
}