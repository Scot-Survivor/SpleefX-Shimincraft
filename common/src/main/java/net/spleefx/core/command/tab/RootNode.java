package net.spleefx.core.command.tab;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RootNode extends TabNode<RootNode> {

    @Getter
    private boolean built;

    RootNode() {
        parent = this;
        level = -1;
    }

    @Override public String getName() {
        return "parent";
    }

    @Override public List<String> supply(String partial) {
        return Collections.emptyList();
    }

    public List<TabNode<?>> getByLevel(int level) {
        return recursiveGetByLevel(children.values(), level, new ArrayList<>());
    }

    private List<TabNode<?>> recursiveGetByLevel(Collection<TabNode<?>> list, int level, List<TabNode<?>> result) {
        for (TabNode<?> node : list) {
            if (node.level > level) break;
            if (node.level == level)
                result.add(node);
            if (!node.children.isEmpty()) {
                recursiveGetByLevel(node.children.values(), level, result);
            }
        }
        return result;
    }

    public RootNode end() {
        built = true;
        assignLevel(this);
        return this;
    }

    private void assignLevel(TabNode<?> node) {
        node.level = node.parent.level + 1;
        for (TabNode<?> child : node.children.values()) assignLevel(child);
    }

}