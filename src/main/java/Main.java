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
        boolean inDoubleQuote = false;
        boolean escaping = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                } else {
                    token.append(c);
                }
            } else if (inDoubleQuote) {
                if (escaping) {
                    if (c == '\\' || c == '"' || c == '$') {
                        token.append(c);
                    } else {
                        token.append('\\').append(c); // keep the backslash
                    }
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inDoubleQuote = false;
                } else {
                    token.append(c);
                }
            } else {
                if (Character.isWhitespace(c)) {
                    if (token.length() > 0) {
                        tokens.add(token.toString());
                        token.setLength(0);
                    }
                } else if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else {
                    token.append(c);
                }
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        return tokens;
    }
}