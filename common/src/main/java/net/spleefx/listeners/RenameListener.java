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
package net.spleefx.listeners;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.gui.ArenaSettingsUI;
import net.spleefx.util.game.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listens for {@link AsyncPlayerChatEvent} to handle arena renaming
 */
@RegisteredListener
public class RenameListener implements Listener {

    public static final String CANCEL = "cancel-edit";

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (ArenaSettingsUI.RENAMING.has(event.getPlayer())) {
            MatchArena arena = ArenaSettingsUI.RENAMING.get(event.getPlayer());
            if (arena == null) return;
            if (event.getMessage().equals(CANCEL)) {
                event.setCancelled(true);
                event.getPlayer().removeMetadata("spleefx.renaming", SpleefX.getPlugin());
                Chat.plugin(event.getPlayer(), "&aRenaming has been cancelled.");
                return;
            }
            arena.setDisplayName(event.getMessage());
            event.setCancelled(true);
            Chat.plugin(event.getPlayer(), "&aDisplay name of arena &e" + arena.getKey() + " &ahas been changed into &d" + event.getMessage());
            event.getPlayer().removeMetadata("spleefx.renaming", SpleefX.getPlugin());
        }
    }


}
