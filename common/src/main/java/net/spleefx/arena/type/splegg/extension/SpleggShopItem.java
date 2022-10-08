package net.spleefx.arena.type.splegg.extension;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.annotations.SerializedName;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Item;
import net.spleefx.util.Placeholders.SpleggPurchaseEntry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpleggShopItem extends Item {

    @SerializedName(value = "PurchaseUpgrade", alternate = "Upgrade")
    private String purchaseUpgrade = "";

    private transient SpleggUpgrade upgrade;

    protected SpleggShopItem(XMaterial type,
                             @Nullable String displayName,
                             List<String> lore,
                             int count,
                             Map<Enchantment, Integer> enchantments,
                             ItemFlag[] itemFlags,
                             @Nullable UUID skull,
                             @Nullable String textureValue,
                             boolean unbreakable,
                             boolean teamColor) {
        super(type, displayName, lore, count, enchantments, itemFlags, skull, textureValue, unbreakable, teamColor);
    }

    public ItemStack create(MatchPlayer player) {
        return withPlaceholders(new SpleggPurchaseEntry(player.getProfile(), getUpgrade()));
    }

    public SpleggUpgrade getUpgrade() {
        if (upgrade == null)
            return upgrade = SpleggUpgrade.get(purchaseUpgrade);
        return upgrade;
    }

}
