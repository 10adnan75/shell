package core;

import java.util.ArrayList;
import java.util.List;

public class ShellHistory {
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    public void add(String command) {
        history.add(command);
        historyIndex = -1;
    }

    public String previous() {
        if (history.isEmpty())
            return "";
        if (historyIndex == -1)
            historyIndex = history.size() - 1;
        else
            historyIndex = Math.max(0, historyIndex - 1);
        return history.get(historyIndex);
    }

    public String next() {
        if (history.isEmpty() || historyIndex == -1)
            return "";
        historyIndex++;
        if (historyIndex >= history.size()) {
            historyIndex = -1;
            return "";
        }
        return history.get(historyIndex);
    }

    public void resetIndex() {
        historyIndex = -1;
    }
}