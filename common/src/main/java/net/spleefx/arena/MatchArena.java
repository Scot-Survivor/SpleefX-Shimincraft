package net.spleefx.arena;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.spleefx.arena.engine.*;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.config.TeamsConfig;
import net.spleefx.core.command.Prefixable;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.json.Keyed;
import net.spleefx.json.KeyedAdapters.ToStringCollection;
import net.spleefx.json.KeyedAdapters.ToStringKeyMap;
import net.spleefx.model.Position;
import net.spleefx.model.ScheduledCommand;
import net.spleefx.powerup.api.Powerup;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.spleefx.extension.StandardExtensions.SPLEGG;

@Getter
@Setter
@GsonHook
public class MatchArena implements Keyed, Prefixable {

    private static final ImmutableSet<MatchTeam> FFA_TEAM = ImmutableSet.of(TeamsConfig.FFA.get());

    @Getter(AccessLevel.NONE)
    protected final String key;

    @SerializedName(value = "Origin", alternate = "RegenerationPoint")
    protected Position origin;

    @SerializedName(value = "Type", alternate = "ArenaType")
    protected final ArenaType type;

    @Setter(AccessLevel.NONE)
    protected transient MatchExtension extension;

    @Setter(AccessLevel.NONE)
    protected List<Position> signs = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @JsonAdapter(ToStringCollection.class)
    protected Set<MatchTeam> teams = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @JsonAdapter(ToStringKeyMap.class)
    protected Map<MatchTeam, Position> spawnPoints = new HashMap<>();

    @JsonAdapter(ToStringKeyMap.class)
    @Setter(AccessLevel.NONE)
    protected Map<MatchTeam, Position> teamLobbies = new HashMap<>();

    @Setter(AccessLevel.NONE)
    private Set<Material> materials;
    private boolean destroyableByDefault = true;

    protected List<Powerup> powerups = new ArrayList<>();

    protected List<ScheduledCommand> scheduledCommands = new ArrayList<>();

    // mutable properties
    protected boolean enabled = true;
    protected boolean dropMinedBlocks = false;

    protected String displayName;

    protected Position finishingLocation;
    protected Position lobby;
    protected Position spectatingPoint;
    protected Position powerupsCenter;

    protected int gameTime = 5;
    protected int powerupsRadius = 0;
    protected int deathLevel = 1;
    protected int minimum = 2;
    protected int maxPlayerCount = 4;
    protected int membersPerTeam = 1;

    @Nullable
    protected XMaterial joinGUIItem;

    // post-init transient properties
    protected transient ReloadedArenaEngine engine;
    protected transient SignHandler signHandler;

    @SerializedName(value = "FfaSettings", alternate = "ffaSettings")
    private SpawnPointHelper ffaSettings;

    public MatchArena(String key, Position origin, ArenaType type, MatchExtension extension) {
        this(key, key, origin, type, extension);
    }

    public MatchArena(String key, String displayName, Position origin, ArenaType type, MatchExtension extension) {
        this.key = key;
        this.displayName = displayName;
        this.origin = origin;
        this.type = type;
        this.extension = extension;
        postInit();
    }

    @AfterDeserialization
    private void postInit() {
        signHandler = new SignHandler(this);
        if (spawnPoints == null) spawnPoints = new HashMap<>();
        if (ffaSettings == null) ffaSettings = new SpawnPointHelper();
        if (teams == null) teams = new HashSet<>();
        if (isFFA()) teams = FFA_TEAM;
        if (signs == null) signs = new ArrayList<>();
        if (displayName == null) displayName = key;
        if (scheduledCommands == null) scheduledCommands = new ArrayList<>();
        if (powerups == null) powerups = new ArrayList<>();
        if (powerupsRadius == 0) powerupsRadius = SpleefXConfig.POWERUPS_RADIUS.get();
        if (getMaterials() == null) {
            materials = new HashSet<>();
            destroyableByDefault = true;
        }
        if (type == ArenaType.FREE_FOR_ALL) engine = new FFAArenaEngine(this);
        else engine = new TeamsArenaEngine(this);
    }

    @Override public @NotNull String getKey() {
        return key;
    }

    public boolean isTeams() {
        return type == ArenaType.TEAMS;
    }

    public boolean isFFA() {
        return type == ArenaType.FREE_FOR_ALL;
    }

    public int getMaximum() {
        return isFFA() ? maxPlayerCount : membersPerTeam * teams.size();
    }

    @Override public @NotNull String getPrefix() {
        return extension.getPrefix();
    }

    public boolean canDestroy(Material material) {
        if (material == Material.TNT && (this instanceof SpleggArena && SPLEGG.getExplodeTNTWhenHit().isEnabled()))
            return true;
        return destroyableByDefault != getMaterials().contains(material);
    }

    @ValueOf
    public static MatchArena getByKey(@NotNull String key) {
        return Arenas.getArenas().get(key);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchArena)) return false;
        MatchArena arena = (MatchArena) o;
        return Objects.equals(key, arena.key);
    }

    @Override public int hashCode() {
        return Objects.hash(key);
    }
}