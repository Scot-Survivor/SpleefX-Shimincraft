package net.spleefx.core.command;

import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown to indicate that the command execution should stop (optionally with a message).
 */
public class CommandException extends Exception {

    @Nullable
    private final String message;
    private boolean fromMessage;

    public CommandException(@Nullable String message, Object... format) {
        this.message = message == null ? null : String.format(message, format);
    }

    public CommandException(Message message, Object... format) {
        this.message = message.create(format).replace("[noprefix]", "");
        fromMessage = true;
    }

    public CommandException() {
        this((String) null);
    }

    public void send(Prefixable prefix, PromptSender sender) {
        if (message == null) return;
        if (fromMessage) prefix = Prefixable.NONE;
        sender.reply(prefix, message);
    }
}