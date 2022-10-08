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
package net.spleefx.v1_8_R3;

import net.spleefx.compatibility.ProtocolNMS;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class ProtocolNMSImpl implements ProtocolNMS {

    @Override public void hidePlayer(Player toHide, Player target) {
        toHide.hidePlayer(target);
    }

    @Override public void setCollidable(Player player, boolean newValue) {
        // not supported
        try {
            player.spigot().setCollidesWithEntities(false);
        } catch (Throwable ignored) {
        }
    }

    @Override public void showPlayer(Player toHide, Player target) {
        toHide.showPlayer(target);
    }

    @Override public double getMaxHealth(Player player) {
        return player.getMaxHealth();
    }

    @Override public org.bukkit.entity.Entity getEntity(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftServer) Bukkit.getServer()).getServer().a(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override public void setUnbreakable(ItemMeta item) {
        try {
            item.spigot().setUnbreakable(true);
        } catch (Throwable ignored) {
        }
    }

}
