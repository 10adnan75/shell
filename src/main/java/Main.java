import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
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

            if (input.isBlank())
                continue;

            List<String> tokenList = tokenize(input);
            if (tokenList.isEmpty())
                continue;

            String[] inputSplit = tokenList.toArray(new String[0]);

            currentDirectory = handler.handleCommand(inputSplit, input, currentDirectory);
        }
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inSingleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (Character.isWhitespace(c) && !inSingleQuote) {
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(c);
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        return tokens;
    }
}