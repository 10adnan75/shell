package builtins;

import java.nio.file.Path;

/**
 * Command.java
 *
 * Interface for all shell commands (builtins and external).
 *
 * Author: Adnan Mazharuddin Shaikh
 */
public interface Command {

    Path execute(String[] args, String rawInput, Path currentDirectory);
}