package core;

import java.util.*;

public class Trie {
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean isWord = false;
    }

    private final Node root = new Node();

    public void insert(String word) {
        Node node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, _ -> new Node());
        }
        node.isWord = true;
    }

    public List<String> getCompletions(String prefix) {
        List<String> results = new ArrayList<>();
        Node node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null)
                return results;
        }
        collect(prefix, node, results);
        return results;
    }

    private void collect(String prefix, Node node, List<String> results) {
        if (node.isWord)
            results.add(prefix);
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            collect(prefix + entry.getKey(), entry.getValue(), results);
        }
    }
}