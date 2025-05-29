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
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d  %s%n", i + 1, history.get(i));
        }
        return currentDirectory;
    }
}