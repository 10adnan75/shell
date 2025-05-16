import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isBlank()) continue;

            String[] inputSplit = input.split(" ");
            
            currentDirectory = handler.handleCommand(inputSplit, input, currentDirectory);
        }
    }
}