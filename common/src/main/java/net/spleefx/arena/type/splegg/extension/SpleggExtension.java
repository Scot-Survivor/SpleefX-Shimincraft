package net.spleefx.arena.type.splegg.extension;

import lombok.Getter;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.MatchExtension.StandardExtension;
import net.spleefx.json.GsonHook;
import net.spleefx.model.ExplosionSettings;
import net.spleefx.model.Item.SlotItem;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@GsonHook
@StandardExtension
public class SpleggExtension extends MatchExtension {

    private SlotItem projectileItem;
    private ProjectileType projectileType;
    private boolean upgradeSystemEnabled;
    private ExplosionSettings explodeTNTWhenHit;

    private List<Action> clickActions;

    private Map<String, SpleggUpgrade> upgrades = new HashMap<>();
    private SpleggShop spleggShop;

}
