package net.spleefx.arena.type.bowspleef;

import net.spleefx.arena.engine.ForwardingArenaEngine;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.extension.ability.GameAbility;
import org.jetbrains.annotations.NotNull;

import static net.spleefx.extension.StandardExtensions.BOW_SPLEEF;

public class BowSpleefEngine extends ForwardingArenaEngine {

    public BowSpleefEngine(ReloadedArenaEngine delegate) {
        super(delegate);
        setExtension(BOW_SPLEEF);
    }

    @Override protected void gameStart(@NotNull MatchPlayer player) {
        super.gameStart(player);
        if (BOW_SPLEEF.getTripleArrows().isEnabled()) {
            int amount = player.getNumericPermission("spleefx.bow_spleef.triple_arrows", BOW_SPLEEF.getTripleArrows().getDefaultAmount());
            getAbilities().get(player.player()).put(GameAbility.TRIPLE_ARROWS, amount);
        }
    }
}
