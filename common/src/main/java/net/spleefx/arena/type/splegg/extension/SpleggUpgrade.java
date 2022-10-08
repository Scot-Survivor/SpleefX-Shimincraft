package net.spleefx.arena.type.splegg.extension;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.spleefx.SpleefX;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.json.Keyed;
import net.spleefx.model.Item;
import net.spleefx.model.Item.SlotItem;
import net.spleefx.util.message.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class SpleggUpgrade implements Keyed {

    private String key = "";
    private String displayName = "";

    private double delay = 1;

    @SerializedName("Default")
    private boolean isDefault = false;

    private int price = 100;

    @SerializedName("RequiredUpgradesBefore")
    private List<String> requiredUpgradesBeforeKeys = Collections.emptyList();
    private SlotItem gameItem = (SlotItem) Item.builder().type(Material.IRON_INGOT).unbreakable(true).slot(0).build();

    @Override
    public @NotNull String getKey() {
        return key;
    }

    public boolean purchase(MatchPlayer player) {
        PlayerProfile profile = player.getProfile(); // these are immutable soo
        PlayerProfile.Builder stats = profile.asBuilder();
        Set<String> upgrades = profile.upgradeKeys();

        AtomicBoolean reloadGUI = new AtomicBoolean();
        // add default upgrades
        StandardExtensions.SPLEGG.getUpgrades().values().stream()
                .filter(upgrade -> upgrade.isDefault() && !upgrades.contains(upgrade.getKey()))
                .forEach(stats::addPurchase);

        if (isDefault || profile.getPurchasedSpleggUpgrades().contains(this)) {
            Message.UPGRADE_SELECTED.reply(player.player(), StandardExtensions.SPLEGG, this);
            stats.setSpleggUpgrade(getKey());
            reloadGUI.set(true);
        } else {
            if (profile.getCoins() >= price) {
                if (profile.upgradeKeys().containsAll(requiredUpgradesBeforeKeys)) {
                    stats.addPurchase(this);
                    stats.setSpleggUpgrade(getKey());
                    stats.subtractCoins(price);
                    Message.UPGRADE_PURCHASED.reply(player.player(), StandardExtensions.SPLEGG, this);
                    reloadGUI.set(true);
                } else {
                    Message.MUST_PURCHASE_BEFORE.reply(player.player(), StandardExtensions.SPLEGG, this);
                }
            } else
                return false;
        }
        stats.push().thenAccept(p -> Bukkit.getScheduler().runTaskLater(SpleefX.getPlugin(), () -> {
            // player.player().closeInventory();
            new SpleggShop.SpleggMenu(StandardExtensions.SPLEGG.getSpleggShop(), player.player());
        }, 1));
        return true;
    }

    @ValueOf
    public static SpleggUpgrade get(@NotNull String key) {
        return StandardExtensions.SPLEGG.getUpgrades().get(key);
    }

}
