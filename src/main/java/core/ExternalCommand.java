package core;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import builtins.Command;

public class ExternalCommand implements Command {
    private final String[] args;
    private final File redirectFile;

    public ExternalCommand(String[] args) {
        this(args, null);
    }

    public ExternalCommand(String[] args, File redirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder(argsToList(args));
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                pb.redirectOutput(redirectFile);
            }

            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();

            if (redirectFile == null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }

            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
        return currentDirectory;
    }

    private List<String> argsToList(String[] args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }
}