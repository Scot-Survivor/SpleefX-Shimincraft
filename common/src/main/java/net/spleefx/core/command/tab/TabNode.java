package net.spleefx.core.command.tab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a tab completion node
 *
 * @param <T> The same implementing type. This is supplied to returning methods to allow
 *            the actual class to get returned in chaining methods.
 */
public abstract class TabNode<T extends TabNode<T>> {

    public final Map<String, TabNode<?>> children = new HashMap<>();

    protected int level = 0;
    protected TabNode<?> parent;

    public abstract String getName();

    public abstract List<String> supply(String partial);

    public T then(TabNode<?> node) {
        if (node instanceof RootNode) throw new IllegalArgumentException("Cannot add RootNode!");
        node.parent = this;
        children.put(node.getName(), node);
        return (T) this;
    }

    public T and(TabNode<?> node) { // better semantically and more readable
        return then(node);
    }

    public T with(TabNode<?> node) { // better semantically and more readable
        return then(node);
    }

    public T then(String node) {
        return then(TabCompletion.literal(node));
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "{" +
                "level=" + level + ", " +
                "name=" + getName() +
                '}';
    }

    public boolean match(String another, String[] args, int index) {
        return getName().equalsIgnoreCase(another);
    }

    public static abstract class UnnamedTabNode<T extends TabNode<T>> extends TabNode<T> {

        @Override public String getName() {
            return "name";
        }
    }

}