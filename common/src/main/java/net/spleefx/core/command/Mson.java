package net.spleefx.core.command;

import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import net.spleefx.compatibility.chat.ChatEvents.HoverEvent;

/**
 * A small, intentionally with short method names utility for building JSON messages
 */
public class Mson extends ChatComponent {

    private Mson(String message) {
        this(message, false);
    }

    private Mson(String message, boolean prefix) {
        setText(message, prefix);
    }

    public Mson suggest(String text) {
        return click(ClickEvent.SUGGEST_COMMAND, text);
    }

    public Mson tooltip(String text) {
        return hover(HoverEvent.SHOW_TEXT, text);
    }

    public Mson execute(String command, Object... format) {
        return click(ClickEvent.RUN_COMMAND, String.format(command, format));
    }

    public Mson url(String url) {
        return click(ClickEvent.OPEN_URL, url);
    }

    public Mson click(ClickEvent action, String value) {
        setClickAction(action, value);
        return this;
    }

    public Mson hover(HoverEvent action, String value) {
        setHoverAction(action, value);
        return this;
    }

    public static Mson of(String message, Object... format) {
        return new Mson(String.format(message, format));
    }

    public static Mson border(String message, Object... format) {
        return of("&7[" + message + "&7]", format);
    }

    public static Mson prefixed(String message, Object... format) {
        return new Mson(String.format(message, format), true);
    }

}