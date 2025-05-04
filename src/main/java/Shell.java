import java.util.Scanner;

class Shell {
    private final Scanner scanner;
    private final CommandHandler commandHandler;

    public Shell() {
        this.scanner = new Scanner(System.in);
        this.commandHandler = new CommandHandler();
    }

    public void start() {
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isBlank()) continue;

            commandHandler.handle(input);
        }
    }
}