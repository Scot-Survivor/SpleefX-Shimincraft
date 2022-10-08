package net.spleefx.core.command.tab;

import net.spleefx.core.command.tab.TabNode.UnnamedTabNode;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TabArguments extends UnnamedTabNode<TabArguments> {

    private static final Function<String, List<String>> EMPTY = c -> Collections.emptyList();
    private final Function<String, List<String>> provider;

    public TabArguments(Function<String, List<String>> provider) {
        this.provider = provider;
    }

    public TabArguments() {
        provider = EMPTY;
    }

    @Override public List<String> supply(String partial) {
        return provider.apply(partial);
    }

    @Override public boolean match(String another, String[] args, int index) {
        index = Math.max(0, index - 1);
        if (parent instanceof RootNode) { // IT WORKS SO DONT TOUCH IT.
            return true;
        }
        return parent.match(args[index], args, index);
    }
}