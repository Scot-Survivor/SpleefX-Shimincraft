package net.spleefx.arena.type.splegg;

import net.spleefx.arena.engine.ForwardingArenaEngine;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.type.splegg.extension.SpleggUpgrade;
import net.spleefx.extension.StandardExtensions;
import org.jetbrains.annotations.NotNull;

import static net.spleefx.extension.StandardExtensions.SPLEGG;

public class SpleggEngine extends ForwardingArenaEngine {

    public SpleggEngine(ReloadedArenaEngine delegate) {
        super(delegate);
        setExtension(StandardExtensions.SPLEGG);
    }

    @Override public void onGracePeriodEnd(@NotNull MatchPlayer player, boolean real) {
        super.onGracePeriodEnd(player, real);
        if (SPLEGG.isUpgradeSystemEnabled()) {
            SpleggUpgrade upgrade = SpleggUpgrade.get(player.getProfile().getSelectedSpleggUpgradeKey());
            upgrade.getGameItem().give(player.player());
        } else {
            SPLEGG.getProjectileItem().give(player.player());
        }
    }
}
