package net.spleefx.core.command;

import lombok.ToString;
import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a command response.
 * <p>
 * This class is thread-safe.
 */
@ToString
public class Response {

    private static final Response USAGE = new Response(ResultType.SEND_USAGE, null);
    private static final Response HELP = new Response(ResultType.SEND_HELP, null);
    private static final Response OK = new Response(ResultType.OK, null);

    public enum ResultType {
        SEND_USAGE,
        SEND_HELP,
        OK
    }

    public final ResultType type;
    public final String message;
    public final Prefixable prefixable;

    private Response(ResultType type, @Nullable String message, @Nullable Prefixable prefixable) {
        this.type = Objects.requireNonNull(type, "type");
        this.message = message;
        this.prefixable = prefixable;
    }

    public Response(ResultType type, String message) {
        this(type, message, null);
    }

    public static Response error() throws CommandException {
        throw new CommandException();
    }

    public static Response error(String message, Object... format) throws CommandException {
        throw new CommandException("&c" + message, format);
    }

    public static Response error(Message message, Object... format) throws CommandException {
        throw new CommandException(message, format);
    }

    public static Response invalidUsage() {
        return USAGE;
    }

    public static Response sendHelp() {
        return HELP;
    }

    public static Response ok() {
        return OK;
    }

    public static Response ignore(Object ignored) { // for semantics
        return OK;
    }

    public static Response ok(String message) {
        return new Response(ResultType.OK, message);
    }

    public static Response ok(String message, Object... format) {
        return new Response(ResultType.OK, String.format(message, format));
    }

    public static Response ok(Prefixable prefixable, String message) {
        return new Response(ResultType.OK, message, prefixable);
    }

    public static Response ok(Prefixable prefixable, String message, Object... format) {
        return new Response(ResultType.OK, String.format(message, format), prefixable);
    }

    public static Response ok(Message message, Object... format) {
        return new Response(ResultType.OK, message.create(format).replace("[noprefix]", ""), Prefixable.NONE);
    }

    public static Response ok(Message message) {
        return new Response(ResultType.OK, message.create().replace("[noprefix]", ""), Prefixable.NONE);
    }
}