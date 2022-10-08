package net.spleefx.model;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.model.Item.SlotItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuitItem extends SlotItem {

    private boolean give = true;
    private boolean leaveArena = true;
    private List<String> runCommandsByPlayer = new ArrayList<>();

    protected QuitItem(XMaterial type,
                       @Nullable String displayName,
                       List<String> lore,
                       int count,
                       Map<Enchantment, Integer> enchantments,
                       ItemFlag[] itemFlags,
                       @Nullable UUID skull,
                       @Nullable String textureValue,
                       boolean unbreakable,
                       boolean teamColor,
                       int slot) {
        super(type, displayName, lore, count, enchantments, itemFlags, skull, textureValue, unbreakable, teamColor, slot);
    }

    @Override protected void giveItem(Player player) {
        if (!give) return;
        super.giveItem(player);
    }

    public boolean isGive() {
        return give;
    }

    public boolean leaveArena() {
        return leaveArena;
    }

    public List<String> getRunCommandsByPlayer() {
        return runCommandsByPlayer;
    }
}
