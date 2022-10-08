package net.spleefx.config;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.spleefx.SpleefX;
import net.spleefx.core.data.StorageType;
import net.spleefx.hook.vault.Economy_SpleefX;
import net.spleefx.model.Title.ToggleableTitle;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;

import static net.spleefx.config.ValueFactory.*;

/**
 * A wrapper for SpleefX's 'config.yml'.
 */
public interface SpleefXConfig {

    /**
     * Whether is async tp enabled or not
     */
    ConfigOption<Boolean> ASYNC_TELEPORT = booleanKey("General.AsyncTeleport", true);

    /**
     * Bungee mode enabled
     */
    ConfigOption<Boolean> BUNGEE_ENABLED = booleanKey("Bungee.Enabled", false);

    /**
     * Bungee mode arena
     */
    ConfigOption<String> BUNGEE_ARENA = stringKey("Bungee.Arena", "");

    /**
     * Server to kick players into
     */
    ConfigOption<String> BUNGEE_KICK_TO_SERVER = stringKey("Bungee.KickToServer", "lobby");

    /**
     * The delay between joining arenas
     */
    ConfigOption<Integer> JOINING_DELAY = integerKey("Arena.JoiningDelay", 0);

    /**
     * The update interval in ticks for arenas
     */
    ConfigOption<Integer> ARENA_UPDATE_INTERVAL = integerKey("Arena.LoopUpdateInterval", 20);

    /**
     * Whether it should damage between teams be cancelled or not.
     */
    ConfigOption<Boolean> ARENA_CANCEL_TEAM_DAMAGE = booleanKey("Arena.CancelTeamDamage", true);

    /**
     * Whether arenas require an empty inventory before joining
     */
    ConfigOption<Boolean> ARENA_REQUIRE_EMPTY_INV = booleanKey("Arena.RequireEmptyInventoryBeforeJoining", false);

    ConfigOption<Boolean> KICK_PLAYERS_ON_DEATH = booleanKey("KickPlayersFromArenaOnDeath", false);

    /**
     * The melting radius
     */
    ConfigOption<Integer> ARENA_MELTING_RADIUS = integerKey("Arena.Melting.Radius", 5);

    /**
     * The melting interval
     */
    ConfigOption<Integer> ARENA_MELTING_INTERVAL = integerKey("Arena.Melting.Interval", 100);

    ConfigOption<Boolean> ARENA_MELTING_IGNORE_X = booleanKey("Arena.Melting.IgnoreX", false);
    ConfigOption<Boolean> ARENA_MELTING_IGNORE_Y = booleanKey("Arena.Melting.IgnoreY", true);
    ConfigOption<Boolean> ARENA_MELTING_IGNORE_Z = booleanKey("Arena.Melting.IgnoreZ", false);

    /**
     * A list of all materials that can be melted
     */
    ConfigOption<List<XMaterial>> ARENA_MELTABLE_BLOCKS = materialList("Arena.Melting.MeltableBlocks");

    /**
     * Whether should arenas regenerate when countdown starts
     */
    ConfigOption<Boolean> ARENA_REGENERATE_BEFORE_COUNTDOWN = booleanKey("Arena.RegenerateBeforeGameStarts", true);

    /**
     * The update interval of scoreboards
     */
    ConfigOption<Integer> SCOREBOARD_UPDATE_INTERVAL = integerKey("Arena.ScoreboardUpdateInterval", 4);

    /**
     * Whether should countdown be displayed on exp bar
     */
    ConfigOption<Boolean> DISPLAY_COUNTDOWN_ON_EXP_BAR = booleanKey("Countdown.DisplayOnExpBar", true);

    /**
     * The countdown on enough players
     */
    ConfigOption<Integer> COUNTDOWN_ON_ENOUGH_PLAYERS = integerKey("Countdown.OnEnoughPlayers", 20);

    /**
     * Whether should a sound be played on each countdown broadcast
     */
    ConfigOption<Boolean> PLAY_SOUND_ON_EACH_BROADCAST_ENABLED = booleanKey("Countdown.PlaySoundOnEachBroadcast.Enabled", true);

    /**
     * Sound to play on each broadcast
     */
    ConfigOption<XSound> PLAY_SOUND_ON_EACH_BROADCAST_SOUND = enumKey("Countdown.PlaySoundOnEachBroadcast.Sound", XSound.BLOCK_LEVER_CLICK);

    /**
     * When to play the sounds
     */
    ConfigOption<List<Integer>> PLAY_SOUND_ON_EACH_BROADCAST_WHEN = intList("Countdown.PlaySoundOnEachBroadcast.PlayWhenCountdownIs");

    /**
     * Title to display on each countdown
     */
    ConfigOption<ToggleableTitle> TITLE_ON_COUNTDOWN = complex("TitleOnCountdown", ToggleableTitle.class);

    /**
     * Numbers of countdown to display
     */
    ConfigOption<Map<Integer, String>> TITLE_ON_COUNTDOWN_NUMBERS = integerMap("TitleOnCountdown.NumbersToDisplay");

    /**
     * Numbers to warn on in time out
     */
    ConfigOption<Map<Integer, String>> TIME_OUT_WARN = integerMap("TimeOut.NumbersToWarnOn");

    /**
     * The "all modes" value in the stats GUI
     */
    ConfigOption<String> ALL_MODES_NAME = stringKey("PlayerGameStatistics.AllModesName", "All Modes");

    /**
     * The max size for the cache
     */
    ConfigOption<Integer> MAX_CACHE_SIZE = integerKey("PlayerGameStatistics.MaximumCacheSize", 1_000);

    /**
     * The storage type
     */
    ConfigOption<StorageType> STORAGE_METHOD = notReloadable(enumKey("PlayerGameStatistics.StorageMethod", StorageType.JSON));

    /**
     * The database address
     */
    ConfigOption<String> DB_HOST = stringKey("PlayerGameStatistics.Database.Host");

    /**
     * The database name
     */
    ConfigOption<String> DB_NAME = stringKey("PlayerGameStatistics.Database.DatabaseName", "minecraft");

    /**
     * The database username
     */
    ConfigOption<String> DB_USER = redact(stringKey("PlayerGameStatistics.Database.Username", "root"));

    /**
     * The database password
     */
    ConfigOption<String> DB_PASSWORD = redact(stringKey("PlayerGameStatistics.Database.Password", ""));

    /**
     * HikariCP's maxLifetime property
     */
    ConfigOption<Integer> HIKARI_MAX_LIFETIME = integerKey("PlayerGameStatistics.Database.Hikari.MaxLifetime", 120_000);

    /**
     * HikariCP's maxPoolSize property
     */
    ConfigOption<Integer> HIKARI_MAX_POOL_SIZE = integerKey("PlayerGameStatistics.Database.Hikari.MaxPoolSize", 10);

    /**
     * Whether should the economy be completely dependant on Vault.
     */
    ConfigOption<Boolean> ECO_USE_VAULT = booleanKey("Economy.GetFromVault", false);

    /**
     * Whether should SpleefX register its Vault hooks or not
     */
    ConfigOption<Boolean> ECO_HOOK_INTO_VAULT = notReloadable(booleanKey("Economy.HookIntoVault", false));

    /**
     * Whether are leaderboards enabled
     */
    ConfigOption<Boolean> LEADERBOARDS = notReloadable(booleanKey("Leaderboards.Enabled"));

    /**
     * The leaderboard format
     */
    ConfigOption<String> LEADERBOARDS_FORMAT = stringKey("Leaderboards.Format", "&d#{pos} &e{player} &7- &b{score}");

    /**
     * Whether should the plugin attempt to patch offline players bug.
     */
    ConfigOption<Boolean> PATCH_OFFLINE_BUG = booleanKey("Leaderboards.AttemptToPatchBukkitOfflinePlayersBug", false);

    /**
     * Whether should the plugin attempt to hook into party plugins
     */
    ConfigOption<Boolean> PARTY_SUPPORT = booleanKey("PartiesSupport", false);

    /**
     * The default power up radius
     */
    ConfigOption<Integer> POWERUPS_RADIUS = integerKey("Powerups.ScatterRadius", 15);

    /**
     * The interval of spawning power ups
     */
    ConfigOption<Integer> SPAWN_POWERUPS_EVERY = integerKey("Powerups.SpawnEvery", 15);

    /**
     * The delay between taking power-ups
     */
    ConfigOption<Integer> DELAY_BETWEEN_TAKING = integerKey("Powerups.DelayBetweenTaking", 10);

    /**
     * Whether does vault exist or not
     */
    boolean VAULT_EXISTS = Bukkit.getPluginManager().getPlugin("Vault") != null;

    /**
     * Whether does vault exist or not
     */
    boolean LUCKPERMS_EXISTS = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;

    /**
     * A list of all options in this configuration class
     */
    List<ConfigOption<?>> OPTIONS = ConfigOption.locateSettings(SpleefXConfig.class);

    /**
     * Loads all options in this class
     *
     * @param initial Whether is this the first time to load or not. Used as a
     *                tiny firewall to protect non-{@link ConfigOption#reloadable} keys.
     */
    static void load(boolean initial) {
        ConfigOption.load(OPTIONS, SpleefX.getPlugin().getConfig(), initial);
    }

    static boolean otherEconomy() {
        return VAULT_EXISTS && ECO_USE_VAULT.get() && !ECO_HOOK_INTO_VAULT.get() && !(SpleefX.getSpleefX().getVaultHandler().getEconomy() instanceof Economy_SpleefX);
    }

}