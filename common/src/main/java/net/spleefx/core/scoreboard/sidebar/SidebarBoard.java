package net.spleefx.core.scoreboard.sidebar;

import lombok.Getter;
import net.spleefx.SpleefX;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class SidebarBoard {

    private static final ChatColor[] c = ChatColor.values();
    private final List<ScoreboardEntry> entries = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private Scoreboard scoreboard;
    private Objective objective;
    private UUID uuid;

    private ScoreboardThread thread;

    public SidebarBoard(Player player, ScoreboardThread thread) {
        this.thread = thread;
        setup(player);
        uuid = player.getUniqueId();
    }

    private void setup(Player player) {
        // Register new scoreboard if needed
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Setup sidebar objective
        objective = scoreboard.registerNewObjective("Default", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String name = getThread().getProvider().getTitle(player);
        if (name != null) { // Update scoreboard
            objective.setDisplayName(name);
            player.setScoreboard(scoreboard);
        }
    }

    public ScoreboardEntry getEntryAtPosition(int pos) {
        if (pos >= entries.size()) {
            return null;
        } else {
            return entries.get(pos);
        }
    }

    public String getUniqueIdentifier() {
        String identifier = getRandomChatColor() + ChatColor.WHITE;

        while (identifiers.contains(identifier)) {
            identifier = identifier + getRandomChatColor() + ChatColor.WHITE;
        }

        if (identifier.length() > 16) {
            return getUniqueIdentifier();
        }

        identifiers.add(identifier);

        return identifier;
    }

    private static String getRandomChatColor() {
        return c[ThreadLocalRandom.current().nextInt(c.length)].toString();
    }

    public void refreshState(Player player) {
        try {
            objective.getName();
        } catch (IllegalStateException e) {
            SpleefX.logger().info("Scoreboard has been unregistered. Re-registering.");
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

            // Setup sidebar objective
            objective = scoreboard.registerNewObjective("Default", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            String name = getThread().getProvider().getTitle(player);
            objective.setDisplayName(name);
            player.setScoreboard(scoreboard);
        }
    }

}
