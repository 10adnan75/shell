import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] inputSplit = input.split(" ");
            
            if (input.equals("exit 0")) {
                System.exit(0);
            } else if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
            } else if (input.startsWith("type")) {
                String cmd = inputSplit[1];
                if (cmd.equals("echo") || cmd.equals("type") || cmd.equals("exit")) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
                    System.out.println(cmd + ": not found");
                }
            } else {
                System.out.println(input + ": command not found");
            }
        }

    }
}
