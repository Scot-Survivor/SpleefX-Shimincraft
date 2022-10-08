package net.spleefx.arena.team;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.TeamsConfig;
import net.spleefx.json.Keyed;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents a team that can be in an arena. This class is immutable, hence is thread-safe.
 */
@ToString
@EqualsAndHashCode
@Getter
public class MatchTeam implements Keyed {

    private String key;
    private String displayName;
    private DyeColor color;

    @SerializedName("ChatColor")
    private ChatColor chatColor;
    private XMaterial itemOnSelectionGUI;

    private MatchTeam(@NotNull String key, @NotNull String displayName, @NotNull DyeColor color, @NotNull ChatColor chatColor, @NotNull XMaterial itemOnSelectionGUI) {
        this.key = key;
        this.displayName = displayName;
        this.color = color;
        this.chatColor = chatColor;
        this.itemOnSelectionGUI = itemOnSelectionGUI;
    }

    public static Builder builder(@NotNull String key) {
        return new Builder(key);
    }

    @ValueOf
    public static MatchTeam get(@NotNull String key) {
        return TeamsConfig.TEAMS.get().get(Objects.requireNonNull(key, "key is null!"));
    }

    public static Collection<MatchTeam> teams() {
        return TeamsConfig.TEAMS.get().values();
    }

    public static class Builder {

        private String key, displayName;
        private DyeColor color = PluginCompatibility.attempt(() -> DyeColor.valueOf("LIGHT_GRAY"), () -> DyeColor.valueOf("SILVER"));
        private ChatColor chatColor = ChatColor.GRAY;
        private XMaterial itemOnSelectionGUI = XMaterial.JUKEBOX;

        private Builder(String key) {
            key(key);
        }

        private Builder(MatchTeam team) {
            key = team.key;
            displayName = team.displayName;
            color = team.color;
            chatColor = team.chatColor;
            itemOnSelectionGUI = team.itemOnSelectionGUI;
        }

        public Builder key(@NotNull String key) {
            this.key = requireNonNull(key, "key");
            return this;
        }

        public Builder displayName(@Nullable String displayName) {
            if (displayName == null) return this;
            this.displayName = displayName;
            return this;
        }

        public Builder color(@Nullable DyeColor color) {
            if (color == null) return this;
            this.color = color;
            return this;
        }

        public Builder chatColor(@Nullable ChatColor color) {
            if (color == null) return this;
            this.chatColor = color;
            return this;
        }

        public Builder itemOnSelectionGUI(@Nullable XMaterial itemOnSelectionGUI) {
            if (itemOnSelectionGUI == null) return this;
            this.itemOnSelectionGUI = itemOnSelectionGUI;
            return this;
        }

        public MatchTeam build() {
            return new MatchTeam(
                    key,
                    displayName == null ? key : displayName,
                    color,
                    chatColor,
                    itemOnSelectionGUI
            );
        }
    }

    @Override public @NotNull String getKey() {
        return key;
    }
}