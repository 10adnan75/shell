import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] inputSplit = input.split(" ");
            
            if (inputSplit[0].equals("exit")) {
                System.exit(0);
            } else if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
            } else if (input.startsWith("type")) {
                String cmd = inputSplit[1];
                if (cmd.equals("echo") || cmd.equals("type") || cmd.equals("exit")) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
                    String[] paths = System.getenv("PATH").split(":");
                    boolean found = false;

                    for (String dir : paths) {
                        File file = new File(dir, cmd);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(cmd + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(cmd + ": not found");
                    }
                }
            } else {
                System.out.println(input + ": command not found");
            }
        }

    }
}
