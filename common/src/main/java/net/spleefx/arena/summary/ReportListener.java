package net.spleefx.arena.summary;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.summary.template.GameSummaryTemplate;
import net.spleefx.event.arena.end.PostArenaEndEvent;
import net.spleefx.event.listen.EventListenerAdapter;
import net.spleefx.util.Placeholders;
import net.spleefx.util.Placeholders.FancyTimeEntry;
import net.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.UUID;

@RegisteredListener(parameters = RegisteredListener.APP)
public class ReportListener extends EventListenerAdapter {

    private final SpleefX plugin;

    public ReportListener(SpleefX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPostArenaEnd(@NotNull PostArenaEndEvent event) {
        if (event.isForcibly()) return;
        GameSummaryTemplate template = plugin.gameSummary;
        GameSummary summary = event.getGameSummary();
        SpleefX.nextTick(template.reportDelay(), () -> {
            for (Entry<UUID, PlayerMatchStats> entry : event.getGameSummary().getPlayerStats().entrySet()) {
                UUID player = entry.getKey();
                @Nullable Player pl = Bukkit.getPlayer(player);
                PlayerMatchStats stats = entry.getValue();
                if (pl != null) {
                    for (String line : template.getSummary().getOrDefault(event.getExtension(), Collections.emptyList())) {
                        line = Placeholders.on(line, event.getArena(), player, stats, new FancyTimeEntry("time_survived", summary.getSurvivalTimes().getOrDefault(player, summary.getLength())));
                        pl.sendMessage(Chat.colorize(line));
                    }
                }
            }
        });
    }
}
