package net.spleefx.extension;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.spleefx.SpleefX;
import net.spleefx.core.command.Prefixable;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.json.Keyed;
import net.spleefx.model.*;
import net.spleefx.model.Item.CommandItem;
import net.spleefx.model.Title.ToggleableTitle;
import net.spleefx.model.ability.DoubleJumpOptions;
import net.spleefx.util.ReflectionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.*;

@Getter
@GsonHook
public class MatchExtension implements Prefixable, Keyed {

    private boolean enabled;
    private String key;
    private String displayName;
    private String chatPrefix;
    private SnowballSettings snowballSettings;

    private Map<Integer, CommandExecution> runCommandsForFFAWinners = emptyMap();

    @SerializedName(value = "RunCommandsForTeamWinners", alternate = "RunCommandsForTeamsWinners")
    private Map<Integer, CommandExecution> runCommandsForTeamWinners = emptyMap();

    private List<PotionEffect> givePotionEffects;

    private boolean preventItemDropping = true;
    private boolean giveDroppedItems = true;

    private DoubleJumpOptions doubleJump = new DoubleJumpOptions();

    private Map<Integer, Item> itemsToAdd = emptyMap(); // DONE
    @SerializedName("CommandItemsToAddInWaiting")
    private Map<Integer, CommandItem> waitingCmdItems = emptyMap(); // DONE
    @SerializedName("CommandItemsToAddInGame")
    private Map<Integer, CommandItem> ingameCmdItems = emptyMap(); // DONE
    private Map<ArmorSlot, Item> armorToAdd = emptyMap(); // DONE

    private Map<GameEvent, ToggleableTitle> gameTitles = emptyMap();
    private List<String> signs = emptyList();

    private GameMode waitingMode = GameMode.ADVENTURE;
    private GameMode ingameMode = GameMode.SURVIVAL;

    private Set<DamageCause> cancelledDamageInWaiting = emptySet();
    private Set<DamageCause> cancelledDamageInGame = emptySet();

    private Set<String> extensionCommands = emptySet();
    private Set<String> allowedCommands = emptySet();

    private Map<ScoreboardType, MatchScoreboard> scoreboard = emptyMap();

    private QuitItem quitItem;

    private GracePeriod gracePeriod = new GracePeriod();
    private boolean denyOpeningContainers = false;
    private boolean denyCrafting = false;

    private List<String> runCommandsWhenGameFills = emptyList();
    private List<String> runCommandsWhenGameStarts = emptyList();

    private Set<Material> removeBlocksWhenPunched = emptySet();

    private Map<Material, List<Item>> customDrops = emptyMap();

    private List<String> runCommandsOnTeamWin = emptyList();
    private List<String> runCommandsOnTeamLose = emptyList();

    private boolean playersBlockProjectiles = false;

    private transient boolean modified = false;
    private transient boolean standard;

    @AfterDeserialization
    private void setDefaults() {
        modified = false;
        standard = getClass().isAnnotationPresent(StandardExtension.class);
        if (customDrops == null) customDrops = emptyMap();
    }

    public MatchExtension setEnabled(boolean enabled) {
        this.enabled = enabled;
        modified = true;
        return this;
    }

    public void reload() {
        ExtensionType type = standard ? ExtensionType.STANDARD : ExtensionType.CUSTOM;
        MatchExtension copy = SpleefX.getSpleefX().getExtensions().parseSneaky(type.of(key));
        ReflectionUtil.merge(copy, this);
    }

    @Override
    public @NotNull String getPrefix() {
        return chatPrefix;
    }

    @Override public @NotNull String getKey() {
        return key;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public SnowballSettings getSnowballSettings() {
        if (snowballSettings == null)
            snowballSettings = new SnowballSettings();
        return snowballSettings;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface StandardExtension {

    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchExtension)) return false;
        MatchExtension extension = (MatchExtension) o;
        return Objects.equals(key, extension.key);
    }

    @ValueOf
    public static MatchExtension get(@NotNull String key) {
        return Extensions.getByKey(key);
    }

    @Override public int hashCode() {
        return Objects.hash(key);
    }
}