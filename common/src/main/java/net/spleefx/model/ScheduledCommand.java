package net.spleefx.model;

import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.MatchArena;
import net.spleefx.backend.Schedulers;
import net.spleefx.util.Placeholders;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ScheduledCommand {

    private String command = "";
    private int runAfter = 100;

    public ScheduledCommand() {
    }

    public ScheduledCommand(String command, int runAfter) {
        this.command = command;
        this.runAfter = runAfter;
    }

    public void schedule(@NotNull MatchArena arena) {
        long started = arena.getEngine().started();
        Schedulers.wait(runAfter, TimeUnit.SECONDS).thenRun(() -> {
            if (arena.getEngine().started() != started) return; // not the same arena session
            if (arena.getEngine().getStage() != ArenaStage.ACTIVE) return; // game ended before this got called
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.on(command, arena));
        });
    }

    public String getCommand() {
        return command;
    }

    public int getRunAfter() {
        return runAfter;
    }
}
