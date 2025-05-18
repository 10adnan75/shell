import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import core.CommandHandler;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");
            System.out.flush();

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                currentDirectory = handler.handleCommand(input, currentDirectory);
            }
        }

        scanner.close();
    }
}