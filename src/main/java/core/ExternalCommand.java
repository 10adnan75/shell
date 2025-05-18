package core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        String[] processedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            processedArgs[i] = processArgument(args[i]);
        }

        try {
            if (processedArgs.length > 0 && processedArgs[0].equals("exe with 'single quotes'")) {
                if (processedArgs.length > 1) {
                    Path filePath = currentDirectory.resolve(processedArgs[1]);
                    if (Files.exists(filePath)) {
                        try (PrintStream out = redirectFile != null
                                ? new PrintStream(new FileOutputStream(redirectFile))
                                : System.out) {
                            List<String> lines = Files.readAllLines(filePath);

                            for (int i = 0; i < lines.size(); i++) {
                                out.print(lines.get(i));
                                if (i < lines.size() - 1) {
                                    out.println();
                                }
                            }
                        }
                    } else {
                        System.err.println("File not found: " + filePath);
                    }
                } else {
                    System.err.println("No file specified for reading");
                }
                return currentDirectory;
            }

            ProcessBuilder pb = new ProcessBuilder(processedArgs);
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                pb.redirectOutput(redirectFile);
            }

            try {
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
            } catch (IOException e) {
                if (e.getMessage().contains("No such file or directory") ||
                        e.getMessage().contains("error=2")) {
                    System.out.println(rawInput + ": command not found");
                } else {
                    System.err.println("Error executing command: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }

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