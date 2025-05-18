package core;

import java.io.*;
import java.nio.file.Path;

import builtins.Command;

public class ExternalCommand implements Command {
    private final String[] args;
    private final File redirectFile;

    public ExternalCommand(String[] args, File redirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        try {
            String[] execArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ((arg.startsWith("\"") && arg.endsWith("\"")) ||
                        (arg.startsWith("'") && arg.endsWith("'"))) {
                    arg = arg.substring(1, arg.length() - 1);
                }
                execArgs[i] = arg;
            }

            ProcessBuilder pb = new ProcessBuilder(execArgs);
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                File parentDir = redirectFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                pb.redirectOutput(redirectFile);
            }

            Process process = pb.start();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            if (redirectFile == null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }

        return currentDirectory;
    }
}