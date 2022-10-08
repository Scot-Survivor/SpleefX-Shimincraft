/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package net.spleefx.gui;

import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.config.json.select.ConfigOpt;
import net.spleefx.config.json.select.SelectionEntry;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Item;
import net.spleefx.util.Placeholders;
import net.spleefx.util.game.Chat;
import net.spleefx.util.menu.Button;
import net.spleefx.util.menu.InventoryUI;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JoinGUI extends InventoryUI {

    /**
     * Creates a new menu
     */
    public JoinGUI(MenuSettings menu, Player player, MatchExtension extension) {
        super(Chat.colorize(menu.title).replace("{player}", player.getName()).replace("{extension}", extension != null ? extension.getDisplayName() : ""), menu.rows);
        AtomicInteger slot = new AtomicInteger();
        cancelAllClicks = true;
        Arenas.getArenas().values()
                .stream()
                .filter(a -> a.getExtension().equals(extension))
                .filter(a -> menu.stagesToDisplay.contains(a.getEngine().getStage()))
                .forEach(a -> createButton(menu, a, slot));
        display(player);
    }

    private void createButton(MenuSettings menu, MatchArena arena, AtomicInteger slot) {
        Item.Builder item = menu.items.get(arena.getEngine().getStage()).asBuilder();
        if (arena.getJoinGUIItem() != null)
            item.type(arena.getJoinGUIItem());
        register(slot.getAndIncrement(), Button.builder().item(item.build().withPlaceholders(arena))
                .close()
                .handle(e -> arena.getEngine().playerJoin(MatchPlayer.wrap((Player) e.getWhoClicked()), false).handle(e.getWhoClicked()))
                .build());
    }

    public static class MenuSettings {

        @ConfigOpt("Menu")
        public static final SelectionEntry<MenuSettings> MENU = new SelectionEntry<>(null);

        private String title;
        private int rows;
        private List<ArenaStage> stagesToDisplay;
        private Map<ArenaStage, Item> items;

        private static String placeholders(String string, MatchArena arena) {
            return Placeholders.on(string, arena);
        }

    }

}
