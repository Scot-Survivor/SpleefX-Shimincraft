package net.spleefx.core.command.tab;

import java.util.Collections;
import java.util.List;

/**
 * Represents a literal suggestion
 */
public class TabString extends TabNode<TabString> {

    public final String literalValue;

    public TabString(String literal) {
        literalValue = literal;
    }

    @Override public String getName() {
        return literalValue;
    }

    @Override public List<String> supply(String partial) {
        return Collections.singletonList(literalValue);
    }

}
