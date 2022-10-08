package net.spleefx.hook.luckperms.context;

import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArenaContextCalculator extends SimpleLPContext {

    @Override
    public void calculate(@NotNull Player player, @NotNull ContextConsumer contextConsumer) {
        MatchArena arena = MatchPlayer.wrap(player).getArena();
        contextConsumer.accept(ARENA, arena == null ? "none" : arena.getKey());
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        for (String arena : Arenas.getArenas().keySet()) {
            builder.add(ARENA, arena);
        }
        builder.add(ARENA, "none");
        return builder.build();
    }

}