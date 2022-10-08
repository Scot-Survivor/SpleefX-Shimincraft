package net.spleefx.spectate;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import net.spleefx.extension.ActionBar;
import net.spleefx.model.Item;
import net.spleefx.model.Item.SlotItem;
import net.spleefx.spectate.SpectatePlayerMenu.MenuData;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class SpectatorSettings {

    private boolean enabled = true;
    private boolean canGetInsidePlayers = true;
    private MenuData spectatePlayerMenu = new MenuData();
    private ActionBar spectatingActionBar = new ActionBar(true, "&1Spectating &e{player}");
    private GameMode gameMode = GameMode.SURVIVAL;

    private SlotItem spectateItem = (SlotItem) Item.builder()
            .slot(0)
            .type(XMaterial.END_PORTAL_FRAME)
            .amount(1)
            .name("&aSpectate Players")
            .lore(Arrays.asList("", "&eClick to spectate other players")).build();

    private SlotItem exitSpectatingItem = (SlotItem) Item.builder()
            .slot(8)
            .type(XMaterial.IRON_DOOR)
            .amount(1)
            .name("&cExit Spectating")
            .build();

    private List<PotionEffect> givePotionEffects = new ArrayList<>();

}