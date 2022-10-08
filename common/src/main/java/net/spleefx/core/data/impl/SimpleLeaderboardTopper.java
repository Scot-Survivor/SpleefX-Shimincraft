package net.spleefx.core.data.impl;

import net.spleefx.core.data.LeaderboardTopper;
import net.spleefx.core.data.OfflinePlayerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleLeaderboardTopper implements LeaderboardTopper {

    private final UUID player;
    private final int score;

    private String name;

    public SimpleLeaderboardTopper(UUID player, int score) {
        this.player = Objects.requireNonNull(player);
        this.score = score;
    }

    @NotNull
    @Override
    public UUID getUUID() {
        return player;
    }

    @Override
    public int getScore() {
        return score;
    }

    @NotNull
    @Override
    public CompletableFuture<String> getPlayer() {
        synchronized (player) {
            return name == null ? OfflinePlayerFactory.getOrRequest(player).thenApply(p -> name = p) : CompletableFuture.completedFuture(name);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLeaderboardTopper that = (SimpleLeaderboardTopper) o;
        return Objects.equals(player, that.player);
    }

    @Override public int hashCode() {
        return Objects.hash(player);
    }

    @Override public String toString() {
        return String.format("SimpleLeaderboardTopper{player=%s, score=%d, name=%s}", player, score, name);
    }
}