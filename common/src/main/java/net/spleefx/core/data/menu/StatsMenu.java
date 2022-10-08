package net.spleefx.core.data.menu;

import lombok.ToString;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.data.GameStatType;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Item;
import net.spleefx.util.Placeholders;
import net.spleefx.util.Placeholders.StatsEntry;
import net.spleefx.util.menu.Button;
import net.spleefx.util.menu.InventoryUI;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a menu
 */
@ToString
public class StatsMenu {

    private String title = "";
    private int rows = 3;
    private Map<Integer, Item> items = Collections.emptyMap();

    public InventoryUI asInventory(OfflinePlayer player, MatchExtension mode) {
        return new Menu(this, player, mode);
    }

    public static class Menu extends InventoryUI {

        public Menu(StatsMenu menu, OfflinePlayer statsOf, MatchExtension ext) {
            super(Placeholders.on(ext == null ? SpleefXConfig.ALL_MODES_NAME.get() : menu.title, ext, statsOf), menu.rows);
            cancelAllClicks = true;
            PlayerProfile stats = PlayerRepository.REPOSITORY.lookup(statsOf);
            Map<GameStatType, Integer> gameStats = ext == null ? stats.getGameStats() : stats.getExtensionStatistics(ext);
            for (Entry<Integer, Item> entry : menu.items.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue().withPlaceholders(new StatsEntry(gameStats), ext);
                register(slot, Button.plain(Item.fromItemStack(item).build()));
            }
        }
    }

}