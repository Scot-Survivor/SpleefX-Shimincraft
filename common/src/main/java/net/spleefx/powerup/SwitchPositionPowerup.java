package net.spleefx.powerup;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.model.Position;
import net.spleefx.powerup.api.Powerup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SwitchPositionPowerup extends Powerup {

    private String switchMessage;

    @Override public void onActivate(@NotNull MatchPlayer player, @NotNull ReloadedArenaEngine engine, @NotNull Position position, @NotNull SpleefX plugin) {
        Position pos = player.getPosition();
        List<MatchPlayer> players = new ArrayList<>(engine.getPlayers());
        players.remove(player);
        if (players.isEmpty()) return;
        MatchPlayer switchWith = random(players);
        Position switchedPos = switchWith.getPosition();
        player.teleport(switchedPos);
        switchWith.teleport(pos);
        if (isNotEmpty(switchMessage)) {
            player.msg(switchMessage.replace("{player}", switchWith.name()), engine);
            switchWith.msg(switchMessage.replace("{player}", player.name()), engine);
        }
    }
}
