package net.spleefx.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.BiConsumer;

public enum ArmorSlot {

    HELMET(PlayerInventory::setHelmet),
    CHESTPLATE(PlayerInventory::setChestplate),
    LEGGINGS(PlayerInventory::setLeggings),
    BOOTS(PlayerInventory::setBoots);

    private final BiConsumer<PlayerInventory, ItemStack> set;

    ArmorSlot(BiConsumer<PlayerInventory, ItemStack> set) {
        this.set = set;
    }

    public void set(Player player, ItemStack item) {
        set.accept(player.getInventory(), item);
    }
}