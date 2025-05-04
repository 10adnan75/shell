import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean again = true;
        Scanner scanner = null;

        while (again) {
            System.out.print("$ ");

            scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            String[] paths = System.getenv("PATH").split(":");

            if (input.isBlank()) {
                continue;
            }

            String[] inputSplit = input.split(" ");
            String command = inputSplit[0];
            String rest = "";

            if (command.equals("exit")) {
                System.exit(0);
            } else if (command.equals("echo")) {
                if (inputSplit.length > 1) {
                    rest = input.substring(5);
                }
                System.out.println(rest);
            } else if (input.startsWith("type")) {
                String cmd = inputSplit[1];
                if (cmd.equals("echo") || cmd.equals("type") || cmd.equals("exit")) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
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
                File executable = null;

                for (String dir : paths) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        executable = file;
                        break;
                    }
                }

                if (executable != null) {
                    try {
                        Process process = new ProcessBuilder(inputSplit).redirectErrorStream(true).start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }

                        process.waitFor();
                    } catch (Exception e) {
                        System.out.println("Error executing command: " + e.getMessage());
                    }
                } else {
                    System.out.println(input + ": command not found");
                }
            }
        }

        scanner.close();
    }
}
