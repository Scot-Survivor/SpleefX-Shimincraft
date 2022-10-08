package net.spleefx.core.command.tab;

import net.spleefx.core.command.tab.TabNode.UnnamedTabNode;

import java.util.List;

/**
 * Represents a more flexible implementation as a list of {@link TabString}.
 */
public class TabStringList extends UnnamedTabNode<TabStringList> {

    private List<String> suggestions;

    public TabStringList(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    @Override public List<String> supply(String partial) {
        return suggestions;
    }

    @Override public boolean match(String another, String[] args, int index) {
        return suggestions.stream().anyMatch(c -> c.equalsIgnoreCase(another));
    }
}