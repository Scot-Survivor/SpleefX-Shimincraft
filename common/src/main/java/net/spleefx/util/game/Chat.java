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
package net.spleefx.util.game;

import net.spleefx.arena.MatchArena;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.message.message.Message;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A little chat utility
 */
public class Chat {

    private static final Pattern HEX = Pattern.compile("#[a-fA-F0-9]{6}");

    public static void plugin(CommandSender sender, String message) {
        sender.sendMessage(Message.PREFIX.create() + colorize(message));
    }

    public static void sendUnprefixed(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void prefix(CommandSender sender, MatchArena arena, String message) {
        prefix(sender, arena.getExtension(), message);
    }

    public static void prefix(CommandSender sender, @Nullable MatchExtension extension, String message) {
        if (extension != null)
            sendUnprefixed(sender, extension.getChatPrefix() + message);
        else plugin(sender, message);
    }

    @Contract("null -> null; !null -> !null")
    public static String colorize(@Nullable String text) {
        if (text == null) return null;
        if (Protocol.supports(16)) {
            Matcher matcher = HEX.matcher(text);
            while (matcher.find()) {
                String color = text.substring(matcher.start(), matcher.end());
                text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color).toString());
                matcher = HEX.matcher(text);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}