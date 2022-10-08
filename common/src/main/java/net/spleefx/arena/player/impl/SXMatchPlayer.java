package net.spleefx.arena.player.impl;

import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.papermc.lib.PaperLib;
import lombok.ToString;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.backend.Schedulers;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.compatibility.packet.ChatPacket;
import net.spleefx.compatibility.packet.LegacyTitlePacket;
import net.spleefx.compatibility.packet.NewTitlePacket;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.command.Mson;
import net.spleefx.core.command.Prefixable;
import net.spleefx.core.data.PlayerProfile;
import net.spleefx.core.data.PlayerProfile.Builder;
import net.spleefx.core.data.PlayerRepository;
import net.spleefx.core.scoreboard.sidebar.SidebarBoard;
import net.spleefx.model.Position;
import net.spleefx.model.Title;
import net.spleefx.model.Title.ToggleableTitle;
import net.spleefx.util.game.Chat;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.message.message.Message;
import net.spleefx.util.plugin.Protocol;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ToString
public final class SXMatchPlayer implements MatchPlayer {

    private static final Map<UUID, MatchPlayer> PLAYERS = Collections.synchronizedMap(new HashMap<>());

    private UUID player;
    private String playerName;
    private MatchArena currentArena;
    private Player playerRef;
    private PlayerState state = PlayerState.NOT_IN_GAME;
    private boolean spectating = false;

    private SXMatchPlayer(Player player) {
        this.player = player.getUniqueId();
        this.playerName = player.getName();
        this.playerRef = player;
    }

    @Override public @NotNull String name() {
        return player().getName();
    }

    @Override public @NotNull UUID uuid() {
        return player().getUniqueId();
    }

    @Override public @Nullable <A extends MatchArena> A getArena() {
        return (A) currentArena;
    }

    @Override public @NotNull MatchPlayer setArena(@Nullable MatchArena arena) {
        currentArena = arena;
        return this;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SXMatchPlayer)) return false;
        SXMatchPlayer that = (SXMatchPlayer) o;
        return Objects.equals(player, that.player);
    }

    @Override public int hashCode() {
        return Objects.hash(player);
    }

    @Override public @NotNull PlayerState getState() {
        return state;
    }

    @Override public @NotNull MatchPlayer setState(@NotNull PlayerState state) {
        this.state = Objects.requireNonNull(state, "state is null!");
        return this;
    }

    @Override public @NotNull PlayerProfile getProfile() {
        return PlayerRepository.REPOSITORY.lookup(player);
    }

    @Override public boolean isSpectating() {
        return spectating;
    }

    @Override public @NotNull MatchPlayer setSpectating(boolean spectating) {
        this.spectating = spectating;
        return this;
    }

    @Override public Player player() {
        Player player = Bukkit.getPlayer(this.player);
        if (player != null) {
            this.playerRef = player;
            playerName = player.getName();
        } /*else {*/
//            SpleefX.getPlugin().warn(
//                    "Player " + playerName + " (" + this.player + ") is offline but has a MatchPlayer reference stored.",
//                    "This should not happen. Please report the following trace to the developers.");
//            Thread.dumpStack();
//        }
        return this.playerRef;
    }

    @Override public MatchPlayer teleport(@NotNull Location location) {
        if (SpleefXConfig.ASYNC_TELEPORT.get())
            PaperLib.teleportAsync(player(), n(location, "location"));
        else
            player().teleport(n(location, "location"));
        return this;
    }

    @Override public MatchPlayer teleport(@NotNull Block location) {
        if (SpleefXConfig.ASYNC_TELEPORT.get())
            PaperLib.teleportAsync(player(), n(location, "block").getLocation());
        else
            player().teleport(n(location, "block").getLocation());
        return this;
    }

    @Override public MatchPlayer teleport(@NotNull Position location) {
        if (SpleefXConfig.ASYNC_TELEPORT.get())
            PaperLib.teleportAsync(player(), n(location, "location").centeredLoc());
        else
            player().teleport(n(location, "location").centeredLoc());
        return this;
    }

    @Override public MatchPlayer title(@NotNull Title title) {
        n(title, "title");
        if (!(title instanceof ToggleableTitle) || ((ToggleableTitle) title).isEnabled())
            title(title.getTitle(), title.getSubtitle() == null ? "" : title.getSubtitle(), title.getFadeIn(), title.getDisplay(), title.getFadeOut());
        return this;
    }

    @Override public MatchPlayer title(@Nullable String title) {
        if (title != null)
            title(title, null);
        return this;
    }

    @Override public MatchPlayer title(@Nullable String title, @Nullable String subtitle) {
        title(title, subtitle, 5, 10, 5);
        return this;
    }

    @Override public MatchPlayer title(@Nullable String title, @Nullable String subtitle, int fadeIn, int display, int fadeOut) {
        title = title == null ? "" : Chat.colorize(title);
        subtitle = subtitle == null ? "" : Chat.colorize(subtitle);
        if (Protocol.supports(17)) {
            NewTitlePacket.reset().sendPacket(player());
            NewTitlePacket.title(WrappedChatComponent.fromText(title)).sendPacket(player());
            NewTitlePacket.subtitle(WrappedChatComponent.fromText(subtitle)).sendPacket(player());
            NewTitlePacket.duration(fadeIn, display, fadeOut).sendPacket(player());
        } else {
            new LegacyTitlePacket().setAction(TitleAction.RESET).sendPacket(player());
            new LegacyTitlePacket()
                    .setTitle(WrappedChatComponent.fromText(title))
                    .setAction(TitleAction.TITLE)
                    .sendPacket(player());
            if (StringUtils.isNotEmpty(subtitle)) {
                new LegacyTitlePacket()
                        .setTitle(WrappedChatComponent.fromText(subtitle))
                        .setAction(TitleAction.SUBTITLE)
                        .sendPacket(player());
            }
            new LegacyTitlePacket()
                    .setAction(TitleAction.TIMES)
                    .setFadeIn(fadeIn)
                    .setStay(display)
                    .setFadeOut(fadeOut)
                    .sendPacket(player());
        }
        return this;
    }

    @Override public MatchPlayer actionBar(@NotNull String text) {
        n(text, "actionBar");
        ChatPacket packet = new ChatPacket();
        packet.setMessage(WrappedChatComponent.fromText(text));
        packet.setChatType(ChatType.GAME_INFO);
        packet.sendPacket(player());
        return this;
    }

    @Override public MatchPlayer giveItems(ItemStack... items) {
        player().getInventory().addItem(items);
        return this;
    }

    @Override public MatchPlayer item(int slot, @NotNull ItemStack itemStack) {
        player().getInventory().setItem(slot, itemStack);
        return this;
    }

    @Override public MatchPlayer msg(@NotNull String message) {
        player().sendMessage(Chat.colorize(n(message, "message")));
        return this;
    }

    @Override public MatchPlayer msg(@NotNull String message, @Nullable Prefixable prefixable) {
        n(message, "message");
        if (prefixable == null) msg(message);
        else msg(prefixable.getPrefix() + message);
        return this;
    }

    @Override public MatchPlayer msg(@NotNull Mson component) {
        return msg(new ComponentJSON().append(component));
    }

    @Override public MatchPlayer msg(@NotNull ComponentJSON component) {
        PluginCompatibility.send(component, player());
        return this;
    }

    @Override public MatchPlayer msg(@NotNull Message message, Object... format) {
        message.reply(player(), format, currentArena);
        return this;
    }

    @Override public MatchPlayer contextMessage(@NotNull String message) {
        return msg(message, currentArena);
    }

    @Override public MatchPlayer execute(@NotNull String command) {
        player().performCommand(command);
        return this;
    }

    @Override public MatchPlayer gamemode(@NotNull GameMode mode) {
        n(mode, "mode");
        player().setGameMode(mode);
        return this;
    }

    @Override public MatchPlayer exp(float level) {
        player().setExp(level);
        return this;
    }

    @Override public MatchPlayer expLevel(int level) {
        player().setLevel(level);
        return this;
    }

    @Override public MatchPlayer health(double value) {
        try {
            player().setHealth(value);
        } catch (Throwable t) {
            player().setHealth(20);
        }
        return this;
    }

    @Override public MatchPlayer foodLevel(int value) {
        try {
            player().setFoodLevel(value);
        } catch (Throwable t) {
            player().setFoodLevel(value);
        }
        return this;
    }

    @Override public MatchPlayer flying(boolean flying) {
        player().setFlying(flying);
        return this;
    }

    @Override public MatchPlayer allowFlying(boolean allowFlying) {
        player().setAllowFlight(allowFlying);
        return this;
    }

    @Override public MatchPlayer fallDistance(float distance) {
        player().setFallDistance(distance);
        return this;
    }

    @Override public MatchPlayer sound(Sound sound) {
        player().playSound(player().getLocation(), sound, 1, 1);
        return this;
    }

    @Override public Collection<PotionEffect> potionEffects() {
        return player().getActivePotionEffects();
    }

    @Override public MatchPlayer addPotions(PotionEffect... effects) {
        for (PotionEffect e : effects) {
            player().addPotionEffect(e);
        }
        return this;
    }

    @Override public MatchPlayer clearPotions() {
        player().getActivePotionEffects().forEach(p -> player().removePotionEffect(p.getType()));
        return this;
    }

    @Override public MatchPlayer displayMainScoreboard() {
        try {
            player().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        } catch (NullPointerException ignored) {
        }
        return this;
    }

    @Override public MatchPlayer removeSidebar() {
        SpleefX.getSpleefX().getScoreboardThread().getBoards().remove(uuid());
        player().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        return this;
    }

    @Override public MatchPlayer renderSidebar(@NotNull SidebarBoard board) {
        SpleefX.getSpleefX().getScoreboardThread().getBoards().put(uuid(), board);
        return this;
    }

    @Override public MatchPlayer renderSidebar() {
        return renderSidebar(new SidebarBoard(player(), SpleefX.getSpleefX().getScoreboardThread()));
    }

    @Override public MatchPlayer setCollidable(boolean collidable) {
        PluginCompatibility.setCollidable(player(), collidable);
        return this;
    }

    @Override public <T> T meta(String key) {
        try {
            return (T) player().getMetadata(key).get(0).value();
        } catch (Exception e) {
            return null;
        }
    }

    @Override public <T> T setMeta(@NotNull String key, @Nullable T value) {
        n(key, "metadata key");
        if (value == null)
            player().removeMetadata(key, getPlugin());
        else
            player().setMetadata(key, new FixedMetadataValue(getPlugin(), value));
        return value;
    }

    @Override public <T> T removeMeta(@NotNull String key) {
        n(key, "metadata key");
        T value = meta(key);
        if (value == null)
            return null;
        player().removeMetadata(key, getPlugin());
        return value;
    }

    @Override public Position getPosition() {
        return Position.at(getLocation());
    }

    @Override public Location getLocation() {
        return player().getLocation();
    }

    @Override public CompletableFuture<Void> async(Consumer<MatchPlayer> task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> {
            task.accept(this);
            future.complete(null);
        });
        return future;
    }

    @Override public void sync(BiConsumer<MatchPlayer, Player> task) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> task.accept(this, player()));
    }

    @Override public CompletableFuture<PlayerProfile> changeStats(BiConsumer<PlayerProfile, Builder> mod) {
        return PlayerRepository.REPOSITORY.apply(uuid(), mod);
    }

    @Override public CompletableFuture<PlayerProfile> changeStats(Consumer<Builder> mod) {
        return PlayerRepository.REPOSITORY.apply(uuid(), (p, b) -> mod.accept(b));
    }

    @SuppressWarnings("ConstantConditions")
    @Override public boolean isInventoryEmpty() {
        PlayerInventory inventory = player().getInventory();
        return Arrays
                .stream(inventory.getContents())
                .noneMatch(itemStack -> itemStack != null && !itemStack.getType().name().contains("AIR")) &&
                Arrays
                        .stream(inventory.getArmorContents())
                        .noneMatch(itemStack -> itemStack != null && !itemStack.getType().name().contains("AIR"));
    }

    @Override public int getBalance() {
        return getProfile().getCoins();
    }

    @Override public int getNumericPermission(@NotNull String node) {
        return getNumericPermission(node, 0);
    }

    @Override public int getNumericPermission(@NotNull String node, int def) {
        n(node, "node is null!");
        Player player = player();
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith(node) && perm.getValue()) {
                String[] slices = perm.getPermission().split("\\.");
                return Integer.parseInt(slices[slices.length - 1]);
            }
        }
        return def;
    }

    @Override public JavaPlugin getPlugin() {
        return SpleefX.getPlugin();
    }

    @Override public @NotNull String getPrefix() {
        return currentArena == null ? Prefixable.PLUGIN.getPrefix() : currentArena.getPrefix();
    }

    @Override public MatchPlayer gui(@NotNull InventoryUI menu) {
        menu.display(player());
        return this;
    }

    @Override public MatchPlayer gui(@NotNull Inventory inventory) {
        player().openInventory(inventory);
        return this;
    }

    @NotNull
    public static MatchPlayer wrap(@NotNull Player player) {
        return PLAYERS.computeIfAbsent(player.getUniqueId(), uuid -> new SXMatchPlayer(player));
    }

    @Override public MatchPlayer clearInventory() {
        player().getInventory().clear();
        return this;
    }

    @Override public MatchPlayer armor(@NotNull ItemStack[] armor) {
        player().getInventory().setArmorContents(armor);
        return this;
    }

    @Override public MatchPlayer fireTicks(int fireTicks) {
        player().setFireTicks(fireTicks);
        return this;
    }

    @Override public ItemStack getMainHand() {
        return PluginCompatibility.attempt(() -> player().getInventory().getItemInMainHand(), () -> player().getItemInHand());
    }

    public static MatchPlayer unregister(Player player) {
        return PLAYERS.remove(player.getUniqueId());
    }

    @RegisteredListener
    public static class MapListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            unregister(event.getPlayer());
        }

    }

    private static <T> T n(T t) {
        return Objects.requireNonNull(t);
    }

    private static <T> T n(T t, String m) {
        return Objects.requireNonNull(t, "Passed a null parameter to a non-null argument: " + m);
    }

//    @Override public int getElo() {
//        return getProfile().getElo();
//    }
}