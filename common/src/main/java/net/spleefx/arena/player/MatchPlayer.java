package net.spleefx.arena.player;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.impl.SXMatchPlayer;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.Mson;
import net.spleefx.core.command.Prefixable;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.scoreboard.sidebar.SidebarBoard;
import net.spleefx.model.Position;
import net.spleefx.model.Title;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.message.message.Message;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MatchPlayer extends Prefixable/*, EloComparable*/ {

    @Contract("null -> null; !null -> !null")
    static MatchPlayer wrap(@Nullable Player player) {
        if (player == null) {
            return null;
        }
//        if (!player.isOnline()) {
//            SXMatchPlayer.unregister(player);
//            return null;
//        }
        return SXMatchPlayer.wrap(player);
    }

    @Contract("null -> null; !null -> !null")
    static MatchPlayer wrap(@Nullable Entity player) {
        return wrap((Player) player);
    }

    /**
     * Returns the player name
     *
     * @return The player name
     */
    @NotNull
    String name();

    /**
     * Returns the player UUID
     *
     * @return The UUID
     */
    @NotNull
    UUID uuid();

    /**
     * Returns the player arena. Null if they are not in any
     *
     * @return The player arena
     */
    <A extends MatchArena> A getArena();

    /**
     * Sets the player arena
     *
     * @param arena New arena. Can be null
     * @return This player instance
     */
    @NotNull
    MatchPlayer setArena(@Nullable MatchArena arena);

    /**
     * Returns the player's current state
     *
     * @return The state
     */
    @NotNull
    PlayerState getState();

    /**
     * Sets the player state
     *
     * @param state New player state
     * @return This player instance
     */
    @NotNull
    MatchPlayer setState(@NotNull PlayerState state);

    /**
     * Returns the player's profile
     *
     * @return The profile
     */
    @NotNull
    PlayerProfile getProfile();

    /**
     * Returns whether is the player spectating right now or not
     *
     * @return ^
     */
    boolean isSpectating();

    /**
     * Sets whether is the player spectating or not
     *
     * @param spectating New value to set
     * @return This player instance
     */
    @NotNull
    MatchPlayer setSpectating(boolean spectating);

    /**
     * Returns the player instance
     *
     * @return The {@link Player} instance of this thing.
     */
    Player player();

    /* since we don't have Kotlin, imagine these as extension functions :( */

    MatchPlayer teleport(@NotNull Location location);

    MatchPlayer teleport(@NotNull Block location);

    MatchPlayer teleport(@NotNull Position location);

    MatchPlayer title(@NotNull Title title);

    MatchPlayer title(@Nullable String title);

    MatchPlayer title(@Nullable String title, @Nullable String subtitle);

    MatchPlayer title(@Nullable String title, @Nullable String subtitle, int fadeIn, int display, int fadeOut);

    MatchPlayer actionBar(@NotNull String text);

    MatchPlayer giveItems(ItemStack... items);

    MatchPlayer item(int slot, @NotNull ItemStack itemStack);

    MatchPlayer msg(@NotNull String message);

    MatchPlayer msg(@NotNull String message, @Nullable Prefixable prefixable);

    MatchPlayer msg(@NotNull ComponentJSON component);

    MatchPlayer msg(@NotNull Mson component);

    MatchPlayer msg(@NotNull Message message, Object... format);

    MatchPlayer contextMessage(@NotNull String message);

    MatchPlayer execute(@NotNull String command); // do not include '/' in the command.

    MatchPlayer gamemode(@NotNull GameMode mode);

    MatchPlayer exp(float level);

    MatchPlayer expLevel(int level);

    MatchPlayer health(double value);

    MatchPlayer foodLevel(int value);

    MatchPlayer flying(boolean flying);

    MatchPlayer allowFlying(boolean allowFlying);

    MatchPlayer fallDistance(float distance);

    MatchPlayer sound(Sound sound);

    Collection<PotionEffect> potionEffects();

    MatchPlayer addPotions(PotionEffect... effects);

    MatchPlayer clearPotions();

    MatchPlayer displayMainScoreboard();

    MatchPlayer removeSidebar();

    MatchPlayer renderSidebar(@NotNull SidebarBoard board);

    MatchPlayer renderSidebar();

    MatchPlayer setCollidable(boolean collidable);

    <T> T meta(String key);

    @Contract("_, null -> null") <T> T setMeta(@NotNull String key, @Nullable T value);

    <T> T removeMeta(@NotNull String key);

    Position getPosition();

    Location getLocation();

    CompletableFuture<Void> async(Consumer<MatchPlayer> task);

    void sync(BiConsumer<MatchPlayer, Player> task);

    CompletableFuture<PlayerProfile> changeStats(BiConsumer<PlayerProfile, PlayerProfile.Builder> mod);

    CompletableFuture<PlayerProfile> changeStats(Consumer<PlayerProfile.Builder> mod);

    boolean isInventoryEmpty();

    int getBalance();

    int getNumericPermission(@NotNull String node);

    int getNumericPermission(@NotNull String node, int def);

    JavaPlugin getPlugin();

    MatchPlayer gui(@NotNull InventoryUI menu);

    MatchPlayer gui(@NotNull Inventory inventory);

    MatchPlayer clearInventory();

    MatchPlayer armor(@NotNull ItemStack[] armor);

    MatchPlayer fireTicks(int fireTicks);

    ItemStack getMainHand();

    default boolean isOffline() {
        return !player().isOnline();
    }

}