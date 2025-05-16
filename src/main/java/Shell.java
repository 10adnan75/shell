import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import builtins.Command;

public class Shell {
    public static void main(String[] args) {
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String rawInput = scanner.nextLine().trim();
            if (rawInput.isBlank()) continue;

            String[] tokens = rawInput.split(" ");
            Command command = CommandHandler.getCommand(tokens, rawInput);
            currentDirectory = command.execute(tokens, rawInput, currentDirectory);
        }
    }
}