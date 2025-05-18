package core;

import java.io.*;
import java.nio.file.Path;

import builtins.Command;

public class ExternalCommand implements Command {
    private final String[] args;
    private final File redirectFile;
    private static final boolean DEBUG = false;

    public ExternalCommand(String[] args, File redirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        String[] processedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            processedArgs[i] = processArgument(args[i]);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(processedArgs);
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                pb.redirectOutput(redirectFile);
            }

            try {
                if (DEBUG) {
                    System.err.println("Executing command: " + String.join(" ", processedArgs));
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

                int exitCode = process.waitFor();
                if (DEBUG) {
                    System.err.println("Process exited with code: " + exitCode);
                }

            } catch (IOException e) {
                if (e.getMessage().contains("No such file or directory") ||
                        e.getMessage().contains("error=2")) {
                    System.err.println(processedArgs[0] + ": command not found");
                } else {
                    System.err.println("Error executing command: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }

        System.out.flush();
        System.err.flush();

        return currentDirectory;
    }

    private String processArgument(String arg) {
        if ((arg.startsWith("\"") && arg.endsWith("\"")) ||
                (arg.startsWith("'") && arg.endsWith("'"))) {
            arg = arg.substring(1, arg.length() - 1);
        }

        return arg.replace("\\'", "'").replace("\\\"", "\"");
    }
}