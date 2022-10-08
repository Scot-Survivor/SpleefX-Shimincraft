/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.compatibility.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.spleefx.compatibility.chat.ChatEvents.ClickAction;
import net.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import net.spleefx.compatibility.chat.ChatEvents.HoverAction;
import net.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import net.spleefx.util.JsonBuilder;
import net.spleefx.util.game.Chat;
import net.spleefx.util.message.message.Message;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.StringJoiner;

/**
 * A class for creating chat messages with clickable and hoverable events
 */
public class ChatComponent {

    private static final String COLOR_CHAR = ChatColor.COLOR_CHAR + "";

    /**
     * A component representing the space
     */
    public static final ChatComponent SPACE = new ChatComponent().setText(" ", false);

    private String text;
    private final ClickAction clickAction = new ClickAction();
    private final HoverAction hoverAction = new HoverAction();

    public String getText() {
        return text;
    }

    public ChatComponent setText(String text, boolean prefix) {
        this.text = fixColors((prefix ? Message.PREFIX.create() : "") + Chat.colorize(text));
        return this;
    }

    public ChatComponent setText(String text) {
        return setText(text, false);
    }

    public ChatComponent setHoverAction(HoverEvent action, String value) {
        hoverAction.action(action.toString()).value(Chat.colorize(value));
        return this;
    }

    /**
     * Sets the click action
     *
     * @param action New action to set
     * @return This component
     */
    public ChatComponent setClickAction(ClickEvent action, String value) {
        clickAction.action(action.toString()).value(value);
        return this;
    }

    public static String fixColors(String e) {
        if (e.equals(" ")) return " ";
        StringJoiner builder = new StringJoiner(" ");
        StringJoiner mock = new StringJoiner(" ");
        for (String part : e.split("\\s")) {
            mock.add(part);
            if (part.startsWith(COLOR_CHAR)) {
                builder.add(part);
                continue;
            }
            part = ChatColor.getLastColors(mock.toString()) + part;
            builder.add(part);
        }
        String result = builder.toString();
        if (e.startsWith(" ")) result = " " + result;
        if (e.endsWith(" ")) result += " ";
        return result;
    }

    public static class Adapter implements JsonSerializer<ChatComponent> {

        @Override
        public JsonElement serialize(ChatComponent component, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonBuilder builder = new JsonBuilder()
                    .map("text", component.text);
            if (!component.clickAction.action.equals("NONE"))
                builder.map("clickEvent", new JsonBuilder().map("action", component.clickAction.action).map("value", component.clickAction.value).build().getAsJsonObject());
            if (!component.hoverAction.action.equals("NONE"))
                builder.map("hoverEvent", new JsonBuilder().map("action", component.hoverAction.action).map("value", component.hoverAction.value).build().getAsJsonObject());
            return builder.build();
        }
    }
}