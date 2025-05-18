import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import core.CommandHandler;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        System.out.print("$ ");
        System.out.flush();

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                currentDirectory = handler.handleCommand(input, currentDirectory);
            }

            System.out.print("$ ");
            System.out.flush();
        }

        scanner.close();
    }
}