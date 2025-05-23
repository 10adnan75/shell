package core;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import builtins.Command;

public class ExternalCommand implements Command {
    private final List<String> args;
    private final File redirectFile;
    private final File stderrRedirectFile;
    private static final boolean DEBUG = false;

    public ExternalCommand(List<String> args, File redirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
        this.stderrRedirectFile = null;
    }

    public ExternalCommand(List<String> args, File redirectFile, File stderrRedirectFile) {
        this.args = args;
        this.redirectFile = redirectFile;
        this.stderrRedirectFile = stderrRedirectFile;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder(this.args);
            pb.directory(currentDirectory.toFile());
            pb.redirectErrorStream(false);

            if (redirectFile != null) {
                if (!redirectFile.getParentFile().exists()) {
                    redirectFile.getParentFile().mkdirs();
                }
                if (!redirectFile.exists()) {
                    redirectFile.createNewFile();
                }
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(redirectFile));
            }

            if (stderrRedirectFile != null) {
                try {
                    if (!stderrRedirectFile.getParentFile().exists()) {
                        stderrRedirectFile.getParentFile().mkdirs();
                    }
                    if (!stderrRedirectFile.exists()) {
                        stderrRedirectFile.createNewFile();
                    }
                    pb.redirectError(ProcessBuilder.Redirect.appendTo(stderrRedirectFile));
                } catch (IOException e) {
                    System.err.println("Error setting up stderr redirection: " + e.getMessage());
                    return currentDirectory;
                }
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

            return currentDirectory;
        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory") ||
                    e.getMessage().contains("error=2")) {
                System.err.println(this.args.get(0) + ": command not found");
            } else {
                System.err.println("Error executing command: " + e.getMessage());
            }
            return currentDirectory;
        } catch (InterruptedException e) {
            System.err.println("Command execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return currentDirectory;
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
            return currentDirectory;
        }
    }

    public void execute(File redirectFile, File stderrRedirectFile, boolean isAppend) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(this.args);
        pb.redirectErrorStream(false);

        if (redirectFile != null) {
            if (!redirectFile.getParentFile().exists()) {
                redirectFile.getParentFile().mkdirs();
            }
            if (!redirectFile.exists()) {
                redirectFile.createNewFile();
            }
            if (isAppend) {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(redirectFile));
            } else {
                pb.redirectOutput(redirectFile);
            }
        }

        if (stderrRedirectFile != null) {
            try {
                if (!stderrRedirectFile.getParentFile().exists()) {
                    stderrRedirectFile.getParentFile().mkdirs();
                }
                if (!stderrRedirectFile.exists()) {
                    stderrRedirectFile.createNewFile();
                }
                if (isAppend) {
                    pb.redirectError(ProcessBuilder.Redirect.appendTo(stderrRedirectFile));
                } else {
                    pb.redirectError(stderrRedirectFile);
                }
            } catch (IOException e) {
                throw new IOException("Error setting up stderr redirection: " + e.getMessage());
            }
        }

        try {
            Process process = pb.start();
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Command execution interrupted", e);
        }
    }

    public List<String> getArgs() {
        return args;
    }
}