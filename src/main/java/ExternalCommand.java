import java.io.*;
import java.nio.file.Path;

import builtins.Command;

public class ExternalCommand implements Command {
    private final String[] args;

    public ExternalCommand(String[] args) {
        this.args = args;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(currentDirectory.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error executing command: " + e.getMessage());
        }

        return currentDirectory;
    }
}