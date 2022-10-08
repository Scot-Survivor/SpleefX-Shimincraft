package net.spleefx.arena.type.splegg.extension;

import net.spleefx.SpleefX;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.extension.StandardExtensions;
import net.spleefx.util.game.Chat;
import net.spleefx.util.menu.Button;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.message.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class SpleggShop {

    private String title = "";
    private int rows = 3;
    private Map<Integer, SpleggShopItem> items = Collections.emptyMap();

    public static class SpleggMenu extends InventoryUI {

        public SpleggMenu(SpleggShop shop, Player p) {
            super(Chat.colorize(shop.title), shop.rows);
            cancelAllClicks = true;
            shop.items.forEach((slot, item) -> register(slot, Button.builder().item(item.create(MatchPlayer.wrap(p)))
                    .handle(e -> {
                        MatchPlayer player = MatchPlayer.wrap((Player) e.getWhoClicked());
                        if (!item.getUpgrade().purchase(player))
                            Message.NOT_ENOUGH_COINS_SPLEGG.reply(player.player(), StandardExtensions.SPLEGG, item.getUpgrade());
                    }).build()));
            Bukkit.getScheduler().runTaskLater(SpleefX.getPlugin(), () -> display(p), 3);
        }
    }
}
