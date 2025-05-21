package core;

import java.io.*;
import java.nio.file.Path;

import builtins.Command;

public class ExternalCommand implements Command {
    private final String[] args;
    private final File redirectFile;
    private final File stderrRedirectFile;
    private static final boolean DEBUG = false;

    public ExternalCommand(String[] args, File redirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
        this.stderrRedirectFile = null;
    }

    public ExternalCommand(String[] args, File redirectFile, File stderrRedirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
        this.stderrRedirectFile = stderrRedirectFile;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder(this.args);
            pb.directory(currentDirectory.toFile());

            if (redirectFile != null) {
                File parentDir = redirectFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                pb.redirectOutput(redirectFile);
            }

            if (stderrRedirectFile != null) {
                File parentDir = stderrRedirectFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                pb.redirectError(stderrRedirectFile);
            }

            if (DEBUG) {
                System.err.println("Executing command: " + String.join(" ", this.args));
            }

            Process process = pb.start();

            if (redirectFile == null) {
                process.getInputStream().transferTo(System.out);
            }
            if (stderrRedirectFile == null) {
                process.getErrorStream().transferTo(System.err);
            }

            int exitCode = process.waitFor();
            if (DEBUG) {
                System.err.println("Process exited with code: " + exitCode);
            }

        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory") ||
                    e.getMessage().contains("error=2")) {
                if (stderrRedirectFile != null) {
                    try (PrintStream ps = new PrintStream(new FileOutputStream(stderrRedirectFile))) {
                        ps.println(this.args[0] + ": command not found");
                    } catch (IOException ex) {
                        System.err.println("Error writing to stderr file: " + ex.getMessage());
                    }
                } else {
                    System.err.println(this.args[0] + ": command not found");
                }
            } else {
                if (stderrRedirectFile != null) {
                    try (PrintStream ps = new PrintStream(new FileOutputStream(stderrRedirectFile))) {
                        ps.println("Error executing command: " + e.getMessage());
                    } catch (IOException ex) {
                        System.err.println("Error writing to stderr file: " + ex.getMessage());
                    }
                } else {
                    System.err.println("Error executing command: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            if (stderrRedirectFile != null) {
                try (PrintStream ps = new PrintStream(new FileOutputStream(stderrRedirectFile))) {
                    ps.println("Command execution interrupted: " + e.getMessage());
                } catch (IOException ex) {
                    System.err.println("Error writing to stderr file: " + ex.getMessage());
                }
            } else {
                System.err.println("Command execution interrupted: " + e.getMessage());
            }
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            if (stderrRedirectFile != null) {
                try (PrintStream ps = new PrintStream(new FileOutputStream(stderrRedirectFile))) {
                    ps.println("Error executing command: " + e.getMessage());
                } catch (IOException ex) {
                    System.err.println("Error writing to stderr file: " + ex.getMessage());
                }
            } else {
                System.err.println("Error executing command: " + e.getMessage());
            }
        }

        System.out.flush();
        System.err.flush();

        return currentDirectory;
    }
}