package net.spleefx.powerup;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.model.Potion;
import net.spleefx.powerup.api.Powerup;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SplashPotionPowerup extends Powerup {

    private Set<Potion> potions;

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) {
        player.giveItems(potions.stream().map(p -> {
            ItemStack i;
            if (!XMaterial.SPLASH_POTION.isSupported()) {
                i = new ItemStack(Material.POTION);
                org.bukkit.potion.Potion pot = new org.bukkit.potion.Potion(PotionType.WATER);
                pot.setSplash(true);
                pot.apply(i);
            } else {
                i = XPotion.buildItemWithEffects(Material.SPLASH_POTION, p.getEffect().getType().getColor(), p.getEffect());
            }
            ItemMeta m = i.getItemMeta();
            m.setDisplayName(ChatColor.AQUA + WordUtils.capitalizeFully(getName(p.getType())));
            i.setItemMeta(m);
            return i;
        }).toArray(ItemStack[]::new));
    }

    private static String getName(@NotNull XPotion potion) {
        switch (potion) {
            case SLOW:
                return "Slowness";
            case FAST_DIGGING:
                return "Haste";
            default:
                return potion.name().toLowerCase().replace("_", " ");
        }
    }

}
