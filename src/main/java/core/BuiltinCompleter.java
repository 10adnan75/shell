package core;

import org.jline.reader.*;
import org.jline.terminal.*;
import java.util.List;

public class BuiltinCompleter {
    public static String readLineWithCompletion(String prompt, String[] builtins) throws java.io.IOException {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        Completer completer = new Completer() {
            @Override
            public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
                String word = line.word();
                for (String builtin : builtins) {
                    if (builtin.startsWith(word)) {
                        candidates.add(new Candidate(builtin + " "));
                    }
                }
            }
        };
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
        String line = reader.readLine(prompt);
        if (line == null)
            return null;
        return line.trim();
    }

    private static void clearLine(int totalLength) {
        System.out.print("\033[2K\r");
        System.out.flush();
    }
}