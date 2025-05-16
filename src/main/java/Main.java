import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isBlank()) continue;

            String[] inputSplit = tokenizeInput(input);
            currentDirectory = handler.handleCommand(inputSplit, input, currentDirectory);
        }
    }

    public static String[] tokenizeInput(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("'([^']*)'|\\S+").matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1)); 
            } else {
                tokens.add(matcher.group()); 
            }
        }

        return tokens.toArray(new String[0]);
    }
}