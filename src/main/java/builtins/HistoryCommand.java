package builtins;

import java.nio.file.Path;
import java.util.List;

public class HistoryCommand implements Command {
    private final List<String> history;

    public HistoryCommand(List<String> history) {
        this.history = history;
    }

    @Override
    public Path execute(String[] args, String rawInput, Path currentDirectory) {
        int start = 0;
        if (args.length > 1) {
            try {
                int n = Integer.parseInt(args[1]);
                start = Math.max(0, history.size() - n);
            } catch (NumberFormatException e) {

            }
        }
        for (int i = start; i < history.size(); i++) {
            System.out.printf("    %d  %s%n", i + 1, history.get(i));
        }
        return currentDirectory;
    }
}