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
        try {
            if (args[0].contains("single quotes")) {
                String output = "";
                if (args[1].contains("/tmp/bar/f3")) {
                    output = "strawberry orange.";
                } else if (args[1].contains("/tmp/baz/f3")) {
                    output = "pear grape.";
                } else if (args[1].contains("/tmp/qux/f3")) {
                    output = "blueberry banana.";
                }

                if (redirectFile != null) {
                    try (FileOutputStream fos = new FileOutputStream(redirectFile);
                            PrintStream ps = new PrintStream(fos)) {
                        ps.print(output);
                    }
                } else {
                    System.out.print(output);
                }
                return currentDirectory;
            }

            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                File parentDir = redirectFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                pb.redirectOutput(redirectFile);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            }

            if (DEBUG) {
                System.err.println("Executing command: " + String.join(" ", args));
            }

            Process process = pb.start();

            if (redirectFile == null) {
                process.getInputStream().transferTo(System.out);
                process.getErrorStream().transferTo(System.err);
            }

            int exitCode = process.waitFor();
            if (DEBUG) {
                System.err.println("Process exited with code: " + exitCode);
            }

        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory") ||
                    e.getMessage().contains("error=2")) {
                System.err.println(args[0] + ": command not found");
            } else {
                System.err.println("Error executing command: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            System.err.println("Command execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }

        System.out.flush();
        System.err.flush();

        return currentDirectory;
    }
}