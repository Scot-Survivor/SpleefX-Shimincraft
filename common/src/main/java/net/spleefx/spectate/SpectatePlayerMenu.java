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
package net.spleefx.spectate;

import com.cryptomorin.xseries.SkullUtils;
import com.google.gson.annotations.Expose;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.event.listen.EventListener;
import net.spleefx.util.Placeholders;
import net.spleefx.util.menu.Button;
import net.spleefx.util.menu.InventoryUI;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.spleefx.SpleefX.getSpectatorSettings;

public class SpectatePlayerMenu extends InventoryUI {

    public SpectatePlayerMenu(MatchArena arena) {
        super(getSpectatorSettings().getSpectatePlayerMenu().getTitle(), getAppropriateSize(arena.getEngine().getPlayers().size()));
        cancelAllClicks = true;
        List<MatchPlayer> alivePlayers = new ArrayList<>(arena.getEngine().getPlayers());
        for (int i = 0; i < alivePlayers.size(); i++) {
            MatchPlayer player = alivePlayers.get(i);
            register(i, Button.builder().close().item(getSpectatorSettings().getSpectatePlayerMenu().playerHeadInfo.apply(player.player()))
                    .handle(e -> spectate(arena, (Player) e.getWhoClicked(), player.player()))
                    .build());
        }
    }

    public static void spectate(@NotNull MatchArena arena, @NotNull Player spectator, @NotNull Player target) {
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setSpectatorTarget(target);
        EventListener.post(new PlayerSpectateAnotherEvent(spectator, target, arena));
    }

    @Override public void display(@NotNull HumanEntity entity) {
        entity.closeInventory();
        super.display(entity);
    }

    public static class MenuData {

        @Expose
        private String title = "&2Spectate players";

        @Expose public PlayerHeadInfo playerHeadInfo = new PlayerHeadInfo();

        public String getTitle() {
            return title;
        }
    }

    public static class PlayerHeadInfo {

        private String displayName = "&2{player}";
        private List<String> lore;

        public ItemStack apply(Player player) {
            ItemStack head = SkullUtils.getSkull(player.getUniqueId());
            ItemMeta meta = Objects.requireNonNull(head.getItemMeta());
            meta.setDisplayName(Placeholders.on(displayName, player));
            meta.setLore(lore.stream().map(l -> Placeholders.on(l, player)).collect(Collectors.toList()));
            head.setItemMeta(meta);
            return head;
        }
    }
}