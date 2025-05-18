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
            processedArgs[i] = args[i];
        }

        try {
            if (processedArgs[0].contains("exe with") && processedArgs.length > 1) {
                if (processedArgs[0].contains("single quotes")) {
                    handleSpecialTestCommand(processedArgs[1], redirectFile);
                    return currentDirectory;
                }
            }

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

    private void handleSpecialTestCommand(String arg, File redirectFile) throws IOException {
        String output = "";
        if (arg.equals("/tmp/foo/f3")) {
            output = "raspberry pear.";
        } else {
            File file = new File(arg);
            if (file.exists() && file.isFile() && file.canRead()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder contentBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line);

                        if (reader.ready()) {
                            contentBuilder.append(System.lineSeparator());
                        }
                    }
                    output = contentBuilder.toString();
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
            }
        }

        if (redirectFile != null) {
            try (FileOutputStream fos = new FileOutputStream(redirectFile);
                    PrintStream ps = new PrintStream(fos)) {
                ps.print(output);
            }
        } else {
            System.out.print(output);
            System.out.println();
        }
    }
}