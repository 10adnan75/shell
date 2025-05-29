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
        int limit = history.size();

        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("history: numeric argument required");
                return currentDirectory;
            }
        }

        int start = Math.max(0, history.size() - limit);
        for (int i = start; i < history.size(); i++) {
            System.out.printf("%5d  %s%n", i + 1, history.get(i));
        }
        
        return currentDirectory;
    }
}