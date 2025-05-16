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

            if (escaping) {
                if (inDoubleQuote) {
                    if (c == '"' || c == '\\' || c == '$' || c == '\n') {
                        token.append(c);
                    } else {
                        token.append('\\').append(c); 
                    }
                } else {
                    token.append(c);
                }
                escaping = false;
                continue;
            }

            if (c == '\\') {
                if (inSingleQuote) {
                    token.append('\\');
                } else {
                    escaping = true;
                }
                continue;
            }

            if (c == '\'') {
                if (inDoubleQuote) {
                    token.append(c); 
                } else {
                    inSingleQuote = !inSingleQuote;
                }
                continue;
            }

            if (c == '"') {
                if (inSingleQuote) {
                    token.append('"'); 
                } else {
                    inDoubleQuote = !inDoubleQuote;
                }
                continue;
            }

            if (Character.isWhitespace(c)) {
                if (inSingleQuote || inDoubleQuote) {
                    token.append(c);
                } else {
                    if (token.length() > 0) {
                        tokens.add(token.toString());
                        token.setLength(0);
                    }
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