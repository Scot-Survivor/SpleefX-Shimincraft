package net.spleefx.config;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.reflect.TypeToken;
import net.spleefx.SpleefX;
import net.spleefx.arena.team.MatchTeam;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.spleefx.config.ValueFactory.complex;

/**
 * A wrapper for SpleefX's "teams.yml"
 */
public interface TeamsConfig {

    /**
     * Represents a reflective {@link java.lang.reflect.ParameterizedType} for Map(k = String, v = MatchTeam)
     */
    Type TYPE_TEAMS = new TypeToken<LinkedHashMap<String, MatchTeam>>() {
    }.getType();

    /**
     * The single FFA team
     */
    ConfigOption<MatchTeam> FFA = complex("FFATeam", MatchTeam.builder("FFA")
            .chatColor(ChatColor.GRAY)
            .displayName("")
            .itemOnSelectionGUI(XMaterial.IRON_AXE)
            .color(DyeColor.BLACK)
            .build());

    /**
     * A map of all teams
     */
    ConfigOption<Map<String, MatchTeam>> TEAMS = complex("Teams", TYPE_TEAMS);

    /**
     * A list of all options in this configuration class
     */
    List<ConfigOption<?>> OPTIONS = ConfigOption.locateSettings(TeamsConfig.class);

    /**
     * Loads all options in this class
     *
     * @param initial Whether is this the first time to load or not. Used as a
     *                tiny firewall to protect non-{@link ConfigOption#reloadable} keys.
     */
    static void load(boolean initial) {
        ConfigOption.load(OPTIONS, SpleefX.getSpleefX().getRelativeFile("teams.yml"), initial);
    }
}
