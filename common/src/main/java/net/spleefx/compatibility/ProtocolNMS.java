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
package net.spleefx.compatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.spleefx.SpleefX;
import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ChatComponent.Adapter;
import net.spleefx.core.command.Mson;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;
import java.util.UUID;

/**
 * An interface to abstract NMS access
 */
public interface ProtocolNMS {

    /**
     * Random used for explosions
     */
    Random RANDOM = new Random();

    /**
     * The GSON used for serializing and deserializing
     */
    Gson CHAT_GSON = new GsonBuilder().registerTypeAdapter(ChatComponent.class, new Adapter())
            .registerTypeAdapter(Mson.class, new Adapter()).create();

    default double getDistanceSq(double posX, double posY, double posZ, double x, double y, double z) {
        double d0 = posX - x;
        double d1 = posY - y;
        double d2 = posZ - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default void hidePlayer(Player toHide, Player target) {
        toHide.hidePlayer(SpleefX.getPlugin(), target);
    }

    default void showPlayer(Player toHide, Player target) {
        toHide.showPlayer(SpleefX.getPlugin(), target);
    }

    default void setCollidable(Player player, boolean newValue) {
        player.setCollidable(newValue);
    }

    default Entity getEntity(UUID uuid) {
        return Bukkit.getEntity(uuid);
    }

    default double getMaxHealth(Player player) {
        return player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    }

    static <E extends Entity> E getEnt(UUID uuid) {
        return (E) PluginCompatibility.getEntity(uuid);
    }

    default void setUnbreakable(ItemMeta item) {
        item.setUnbreakable(true);
    }
}
