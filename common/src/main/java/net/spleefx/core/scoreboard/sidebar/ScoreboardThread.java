package net.spleefx.core.scoreboard.sidebar;

import lombok.Getter;
import lombok.Setter;
import net.spleefx.core.scoreboard.ScoreboardProvider;
import net.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ScoreboardThread extends Thread {

    private ScoreboardProvider provider = new ScoreboardProvider();
    private Map<UUID, SidebarBoard> boards = new ConcurrentHashMap<>();
    @Setter private long ticks = 2;

    @Override
    public void run() {
        while (true) {
            try {
                tick();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                sleep(ticks * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SidebarBoard board = boards.get(player.getUniqueId());
            if (board == null) continue;
            board.refreshState(player);

            Objective objective = board.getObjective();
            String title = provider.getTitle(player);
            if (title == null) {
                boards.remove(player.getUniqueId());
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                return;
            }
            if (!objective.getDisplayName().equals(title)) {
                objective.setDisplayName(title);
            }
            List<String> newLines = provider.getLines(player);
            if (newLines == null || newLines.isEmpty()) {
                board.getEntries().forEach(ScoreboardEntry::remove);
                board.getEntries().clear();
            } else {
                Collections.reverse(newLines);

                if (board.getEntries().size() > newLines.size()) {
                    for (int i = newLines.size(); i < board.getEntries().size(); i++) {
                        ScoreboardEntry entry = board.getEntryAtPosition(i);

                        if (entry != null) {
                            entry.remove();
                        }
                    }
                }

                int cache = 1;
                for (int i = 0; i < newLines.size(); i++) {
                    ScoreboardEntry entry = board.getEntryAtPosition(i);

                    String line = Chat.colorize(newLines.get(i));
                    if (entry == null) {
                        entry = new ScoreboardEntry(board, line);
                    }
                    entry.setText(line);
                    entry.setup();
                    entry.send(cache++);
                }
            }
        }
    }
}