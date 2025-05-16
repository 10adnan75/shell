import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler();
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isBlank())
                continue;

            List<String> tokensList = new ArrayList<>();
            Matcher matcher = Pattern.compile("'([^']*)'|\\S+").matcher(input);
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    tokensList.add(matcher.group(1));
                } else {
                    tokensList.add(matcher.group());
                }
            }

            if (tokensList.isEmpty())
                continue;

            String[] inputSplit = tokensList.toArray(new String[0]);
            currentDirectory = handler.handleCommand(inputSplit, input, currentDirectory);
        }
    }
}