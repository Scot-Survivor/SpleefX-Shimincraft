package net.spleefx.core.command;

import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element that can have a prefix
 */
@FunctionalInterface
public interface Prefixable {

    /**
     * The plugin's prefix
     */
    Prefixable PLUGIN = Message.PREFIX::create;

    /**
     * No prefix
     */
    Prefixable NONE = () -> "";

    /**
     * Returns the prefix of this.. thing
     *
     * @return The prefix.
     */
    @NotNull
    String getPrefix();

}
