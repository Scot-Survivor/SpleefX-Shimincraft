package net.spleefx.hook.luckperms.context;

import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.spleefx.arena.player.MatchPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerStateContextCalculator extends SimpleLPContext {

    private static final ContextSet COMPLETIONS = ImmutableContextSet.builder()
            .add(STATE, "waiting")
            .add(STATE, "not_in_game")
            .add(STATE, "playing")
            .add(STATE, "spectating")
            .build();

    @Override
    public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
        MatchPlayer player = MatchPlayer.wrap(target);
        consumer.accept(STATE, player.getState().name().toLowerCase());
    }

    @Override public ContextSet estimatePotentialContexts() {
        return COMPLETIONS;
    }
}
