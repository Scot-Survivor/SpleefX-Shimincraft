package net.spleefx.arena.engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import net.spleefx.SpleefX;
import net.spleefx.arena.ArenaStage;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.player.PlayerState;
import net.spleefx.arena.summary.GameSummary;
import net.spleefx.arena.summary.PlayerMatchStats;
import net.spleefx.arena.summary.PlayerMatchStats.Builder;
import net.spleefx.backend.DelayContext;
import net.spleefx.collect.EntityMap;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.scoreboard.ScoreboardType;
import net.spleefx.event.arena.end.PostArenaEndEvent;
import net.spleefx.event.arena.end.PreArenaEndEvent;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerLoseEvent;
import net.spleefx.event.player.PlayerQuitArenaEvent;
import net.spleefx.event.player.PlayerWinGameEvent;
import net.spleefx.extension.GameEvent;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.MatchScoreboard;
import net.spleefx.extension.ability.GameAbility;
import net.spleefx.hook.parties.PartyHook;
import net.spleefx.hook.parties.SXParty;
import net.spleefx.model.ArmorSlot;
import net.spleefx.model.Item;
import net.spleefx.model.Item.CommandItem;
import net.spleefx.model.ScheduledCommand;
import net.spleefx.model.Title.ToggleableTitle;
import net.spleefx.model.ability.DoubleJumpItems;
import net.spleefx.model.ability.DoubleJumpOptions;
import net.spleefx.powerup.api.PowerupLifecycle;
import net.spleefx.spectate.SpectatePlayerMenu;
import net.spleefx.spectate.Spectating;
import net.spleefx.spectate.Spectating.SpectatingCause;
import net.spleefx.util.ListenableProperty;
import net.spleefx.util.Placeholders;
import net.spleefx.util.Placeholders.FancyTimeEntry;
import net.spleefx.util.Placeholders.StatsEntry;
import net.spleefx.util.message.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.spleefx.SpleefX.getPlugin;
import static net.spleefx.SpleefX.getSpectatorSettings;
import static net.spleefx.arena.engine.ReloadedArenaEngine.JoinResult.allow;
import static net.spleefx.arena.engine.ReloadedArenaEngine.JoinResult.deny;
import static net.spleefx.arena.engine.SXRunnable.isRunning;
import static net.spleefx.backend.Schedulers.DELAY;
import static net.spleefx.config.SpleefXConfig.ARENA_REQUIRE_EMPTY_INV;
import static net.spleefx.config.SpleefXConfig.COUNTDOWN_ON_ENOUGH_PLAYERS;
import static net.spleefx.core.scoreboard.ScoreboardType.WAITING_IN_LOBBY;
import static net.spleefx.util.Placeholders.make;
import static net.spleefx.util.Util.n;

/**
 * A bare implementation for {@link ReloadedArenaEngine} with more convenient, call-back methods
 */
public abstract class AbstractArenaEngine extends ReloadedArenaEngine {

    protected GameSummary.Builder gameSummary;
    protected final MatchArena arena; // the arena
    protected MatchExtension extension; // the arena

    final Set<MatchPlayer> players = new CopyOnWriteArraySet<MatchPlayer>() {
        @Override public void clear() {
            super.clear();
            getArena().getSignHandler().update();
        }

        @Override public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (b) getArena().getSignHandler().update();
            return b;
        }

        @Override public boolean add(MatchPlayer player) {
            boolean b = super.add(player);
            if (b) getArena().getSignHandler().update();
            return b;
        }
    };

    protected final Set<MatchPlayer> spectators = new HashSet<>(); // all spectators
    protected final Set<MatchPlayer> extras = new HashSet<>();
    private final Total total = new Total();
    private final Map<Integer, PowerupLifecycle> activePowerUps = new ConcurrentHashMap<>();
    protected final AbilityHandler abilityHandler = new AbilityHandler();

    protected final EntityMap<Player, Builder> stats = EntityMap.hashMap(); // player stats

    // mutable properties

    protected final AtomicInteger countdown = new AtomicInteger(COUNTDOWN_ON_ENOUGH_PLAYERS.get());
    protected final AtomicInteger timeLeft = new AtomicInteger(0);
    protected final AtomicBoolean gracePeriod = new AtomicBoolean(false);

    private final ListenableProperty<ArenaStage> stage =
            ListenableProperty.of(ArenaStage.WAITING, () -> getArena().getSignHandler().update());

    protected ScoreboardType currentScoreboard = WAITING_IN_LOBBY;
    protected volatile long started;

    // game tasks
    protected CountdownTask countdownTask;
    protected ArenaLoopTask loopTask;
    protected @Nullable PowerupsTask powerupsTask;

    public AbstractArenaEngine(@NotNull MatchArena arena) {
        this.arena = Objects.requireNonNull(arena, "arena is null!");
        this.extension = arena.getExtension();
    }

    @Override public MatchArena getArena() {
        return arena;
    }

    @Override public ArenaStage getStage() {
        if (!arena.isEnabled() || !extension.isEnabled()) {
            return stage.set(ArenaStage.DISABLED);
        }
        if (!isReady())
            return stage.set(ArenaStage.NEEDS_SETUP);
        if (stage.get() == null || stage.get() == ArenaStage.NEEDS_SETUP || (stage.get() == ArenaStage.DISABLED && arena.isEnabled() && extension.isEnabled()))
            return stage.set(ArenaStage.WAITING);
        return stage.get();
    }

    @Override public final void setStage(@NotNull ArenaStage newStage) {
        this.stage.set(n(newStage, "newStage"));
    }

    @Override public final @NotNull JoinResult playerJoin(@NotNull MatchPlayer player, boolean summoned, boolean doJoin) {
        if (DELAY.hasDelay(player, DelayContext.JOIN)) {
            return deny(Message.MUST_WAIT_BEFORE_JOINING, arena, extension);
        }
        if (player.getArena() != null) {
            return deny(Message.ALREADY_IN_ARENA, arena, extension);
        }
        if (stage.get() == ArenaStage.DISABLED || !arena.isEnabled() || !extension.isEnabled()) {
            return deny(Message.ARENA_DISABLED, arena, extension);
        }
        if (ARENA_REQUIRE_EMPTY_INV.get() && !player.isInventoryEmpty()) {
            return deny(Message.MUST_HAVE_EMPTY_INV, arena, extension);
        }
        if (isFull() && stage.get().isPlayable()) {
            return deny(Message.ARENA_FULL, arena, extension);
        }
        SXParty party = PartyHook.current().getParty(player);
        if (party.isReal()) {
            boolean leader = party.isLeader(player);
            List<MatchPlayer> members = party.getPlayers();
            if (leader && !summoned) {
                if (players.size() + members.size() > arena.getMaximum()) {
                    return deny(Message.PARTY_NOT_ENOUGH_SPACE, arena);
                } else {
                    for (MatchPlayer p : party) {
                        if (p.getState() != PlayerState.NOT_IN_GAME) {
                            return deny(Message.PARTY_MEMBERS_IN_GAME, arena);
                        }
                    }
                    for (MatchPlayer p : party) if (p != player) getEngine().playerJoin(p, true, doJoin).handle(p);
                }
            } else if (!summoned) {
                return deny(Message.PARTY_NOT_LEADER, arena);
            }
        }
        switch (getStage()) {
            case ACTIVE: {
                if (!getSpectatorSettings().isEnabled()) {
                    return deny(Message.ARENA_ALREADY_ACTIVE, arena, extension);
                }
                if (!doJoin) return allow();
                player.setArena(arena).setState(PlayerState.SPECTATING);
                player.setSpectating(true);
                PreGamePlayerData data = storeData(player);
                if (players.isEmpty()) {
                    return deny(Message.ARENA_HAS_NO_PLAYERS, arena, extension);
                }
                SpleefX.nextTick(() -> {
                    data.capture(player);
                    getEngine().playerSpectate(player, SpectatingCause.JOINED);
                }).thenRun(() -> SpleefX.nextTick(() -> {
                    if (arena.getSpectatingPoint() == null) {
                        LinkedList<MatchPlayer> availablePlayers = new LinkedList<>(players);
                        Collections.shuffle(availablePlayers);
                        SpectatePlayerMenu.spectate(arena, player.player(), availablePlayers.pop().player());
                    } else {
                        player.teleport(arena.getSpectatingPoint());
                    }
                }));
                renderSidebar(player);
                return allow();
            }
            case REGENERATING:
                return deny(Message.ARENA_REGENERATING, arena, extension);
            case NEEDS_SETUP:
                return deny(Message.ARENA_NEEDS_SETUP, arena, extension);
        }
        if (!doJoin) return allow();
        DELAY.delay(player, DelayContext.JOIN, SpleefXConfig.JOINING_DELAY.get(), TimeUnit.SECONDS);
        boolean added = players.add(player);
        if (!added) return deny("&cYou are already in this arena!");
        player.setArena(arena).setState(PlayerState.WAITING);
        PreGamePlayerData data = storeData(player);
        prepare0(player).thenRun(() -> SpleefX.nextTick(() -> {
            data.capture(player);
            getEngine().onPlayerJoin(player);
        }));
        if (players.size() >= arena.getMinimum() && !isRunning(countdownTask)) {
            countdown();
        }
        renderSidebar(player);
        return allow();
    }

    @Override public final void playerLeave(@NotNull MatchPlayer player, boolean disconnect, boolean urgent) {
        players.remove(player);
        player.setArena(null);
        boolean restore = true;
        if ((getStage() == ArenaStage.ACTIVE)) {
            if (player.isSpectating())
                getEngine().playerLeaveSpectating(player);
            else {
                getEngine().playerEliminated(player, disconnect);
                restore = false;
            }
        } else if (getStage() != ArenaStage.ACTIVE) {
            stats.remove(player.player());
        }
        if (restore)
            restoreData(player, urgent);
        if (getStage() == ArenaStage.COUNTDOWN && players.size() < arena.getMinimum()) {
            countdownTask.cancel();
            countdown.set(SpleefXConfig.COUNTDOWN_ON_ENOUGH_PLAYERS.get());
            setStage(ArenaStage.WAITING);
            currentScoreboard = WAITING_IN_LOBBY;
            for (MatchPlayer p : players) {
                p.msg(Message.NOT_ENOUGH_PLAYERS, arena, player);
                renderSidebar(p);
            }
        }
        if (disconnect)
            EventListener.post(new PlayerQuitArenaEvent(player.player(), arena));
        getEngine().onPlayerLeave(player, disconnect);
    }

    @Override
    protected final void gameStart(@NotNull MatchPlayer player) {
        player.setArena(arena).setState(PlayerState.PLAYING);
        player.clearInventory();
        handleDoubleJumps(player);
        getStats(player).gamePlayed();
        if (!extension.isPlayersBlockProjectiles()) {
            player.setCollidable(false);
        }
        getEngine().onGameStart(player);
        if (!extension.getGracePeriod().isEnabled()) {
            arena.getEngine().onGracePeriodStart(player, false);
            arena.getEngine().onGracePeriodEnd(player, false);
        }
        for (PotionEffect effect : extension.getGivePotionEffects()) {
            player.addPotions(effect);
        }
    }

    @Deprecated
    @Override public void playerSpectate(@NotNull MatchPlayer player, @NotNull SpectatingCause cause) {
        if (this.getStage() != ArenaStage.ACTIVE) return;
        spectators.add(player);
        Spectating.on(player);
        if (cause == SpectatingCause.DIED) {
            if (arena.getSpectatingPoint() != null)
                player.teleport(arena.getSpectatingPoint());
        }
    }

    @Override public final boolean isGracePeriodActive() {
        return gracePeriod.get();
    }

    public final void playerWin(@NotNull MatchPlayer winner) {
        ToggleableTitle title = extension.getGameTitles().get(GameEvent.WIN);
        if (title != null) {
            for (MatchPlayer player : total())
                player.title(title.withPlaceholders(make("winner", winner.name())));
        }
        getStats(winner).win();
        gameSummary.survive(winner).win(winner);
        getEngine().onPlayerWin(winner);
        EventListener.post(new PlayerWinGameEvent(winner.player(), arena));
        restoreData(winner, false);
    }

    @Deprecated
    @Override public void playerLeaveSpectating(@NotNull MatchPlayer player) {
        spectators.remove(player);
        players.remove(player);
        extras.remove(player);
        player.setArena(null);
        Spectating.disable(player);
    }

    private long lastFill;

    @Override public final void countdown() {
        if (isRunning(countdownTask)) return;
        stage.set(ArenaStage.COUNTDOWN);
        countdownTask = new CountdownTask(this);
        countdownTask.runTaskTimer(getPlugin(), 20, 20);
        countdownTask.thenRun(this::start);
        if ((System.currentTimeMillis() - lastFill) > 10000) {
            for (String command : extension.getRunCommandsWhenGameFills()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.on(command, arena));
            }
            lastFill = System.currentTimeMillis();
        }
    }

    @Override public final void start() {
        stage.set(ArenaStage.ACTIVE);
        gameSummary = GameSummary.start(arena);
        for (MatchPlayer player : players) {
            prepareForGame0(player).thenRun(() -> getEngine().gameStart(player));
        }
        started = System.currentTimeMillis();
        loopTask = new ArenaLoopTask(this).schedule();
        if (arena.getPowerupsCenter() != null && !arena.getPowerups().isEmpty())
            powerupsTask = new PowerupsTask(this).schedule();
        for (ScheduledCommand scheduledCommand : arena.getScheduledCommands()) {
            scheduledCommand.schedule(arena);
        }
        for (String command : extension.getRunCommandsWhenGameStarts()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholders.on(command, arena));
        }
        getEngine().onGameStart();
    }

    @Override public final void draw() {
        loopTask.cancel();
        for (MatchPlayer player : this) {
            if (player.getArena() != null && player.getArena().getKey().equals(arena.getKey())) {
                restoreData(player, false);
                player.title(extension.getGameTitles().get(GameEvent.DRAW));
            }
            getStats(player).draw();
        }
        end(false);
    }

    public final void playerEliminated(@NotNull MatchPlayer player, boolean disconnect) {
        players.remove(player);
        gameSummary.survive(player);
        boolean dstats = !disconnect && getSpectatorSettings().isEnabled() && !isSingleUnitAlive();
        if (dstats) {
            playerSpectate(player, SpectatingCause.DIED);
        } else {
            abilityHandler.remove(player.player());
            spectators.remove(player);
            if (!disconnect) extras.add(player);
            restoreData(player, true);
        }
        getEngine().onPlayerEliminate(player);
        if (!dstats) {
            PlayerMatchStats playerStats = stats.remove(player.player()).build();
            playerStats.push(player, extension);
            gameSummary.stat(player, playerStats);
        }
        EventListener.post(new PlayerLoseEvent(player.player(), arena));
    }

    @Override
    public final void end(boolean force) {
        if (force) {
            for (MatchPlayer player : this) {
                if (Objects.equals(player.getArena(), arena)) {
                    player.msg(Message.SERVER_STOPPED, arena);
                    restoreData(player, true);
                }
            }
        }
        if (powerupsTask != null)
            powerupsTask.cancel();
        getEngine().onPreEnd(force);
        for (Entry<Player, Builder> ps : stats.entrySet()) {
            Player player = ps.getKey();
            PlayerMatchStats stat = ps.getValue().build();

            if (player != null) {
                gameSummary.stat(player, stat);
                stat.push(MatchPlayer.wrap(player), extension);
            }
        }
        GameSummary summary = null;
        if (!force)
            summary = gameSummary.build();

        ImmutableList<MatchPlayer> trackedCopy = new ImmutableList.Builder<MatchPlayer>()
                .addAll(this)
                .addAll(extras)
                .build();

        EventListener.post(new PreArenaEndEvent(arena, force, trackedCopy, summary));
        // PRE END
        stats.clear();
        players.clear();
        abilityHandler.clear();
        for (MatchPlayer spectator : spectators) {
            Spectating.disable(spectator);
            restoreData(spectator, force);
        }
        EventListener.post(new PostArenaEndEvent(arena, force, trackedCopy, summary));
        spectators.clear();
        extras.clear();
        getEngine().onPostEnd(force);
        currentScoreboard = WAITING_IN_LOBBY;
        regenerate().thenRun(() -> setStage(ArenaStage.WAITING));
    }

    protected final PreGamePlayerData storeData(@NotNull MatchPlayer player) {
        return player.setMeta("arena.player_context", new PreGamePlayerData(player.player()));
    }

    protected final CompletableFuture<Void> restoreData(@NotNull MatchPlayer player, boolean urgent) {
        PreGamePlayerData playerData = player.removeMeta("arena.player_context");
        if (playerData == null) return CompletableFuture.completedFuture(null);
        getEngine().onContextLoad(player);
        player.removeSidebar();
        player.setState(PlayerState.NOT_IN_GAME).setArena(null);
        return playerData.load(player, arena, urgent);
    }

    protected final void renderSidebar(@NotNull MatchPlayer player) {
        MatchScoreboard scoreboard = extension.getScoreboard().get(currentScoreboard);
        if (scoreboard == null || !scoreboard.isEnabled()) return;
        scoreboard.createScoreboard(player);
    }

    @Override public final @NotNull CompletableFuture<@Nullable Void> regenerate() {
        ArenaStage oldStage = getStage();
        setStage(ArenaStage.REGENERATING);
        return Arenas.regenerateArena(arena).thenRun(() -> setStage(oldStage));
    }

    public final boolean isFull() {
        return players.size() >= arena.getMaximum();
    }

    public final ScoreboardType getCurrentScoreboard() {
        return currentScoreboard;
    }

    public final Builder getStats(@NotNull Player player) {
        return stats.computeIfAbsent(player, p -> PlayerMatchStats.builder());
    }

    @Override public final void clearStats() {
        stats.clear();
    }

    @Nullable
    public Collection<MatchPlayer> checkForWinners() {
        if (players.size() <= 1) return players;
        return null;
    }

    public final Builder getStats(@NotNull MatchPlayer player) {
        return getStats(player.player());
    }

    @Override public final AbilityHandler getAbilities() {
        return abilityHandler;
    }

    @Override public final Set<MatchPlayer> getPlayers() {
        return players;
    }

    @Override public final Set<MatchPlayer> getSpectators() {
        return spectators;
    }

    @Override public final @NotNull String getPrefix() {
        return extension.getPrefix();
    }

    protected final void handleDoubleJumps(@NotNull MatchPlayer player) {
        DoubleJumpOptions doubleJumpSettings = extension.getDoubleJump();
        if (!doubleJumpSettings.isEnabled()) return;
        int amount = player.getNumericPermission("spleefx." + extension.getKey() + ".double_jump", doubleJumpSettings.getDefaultAmount());
        if (amount > 0) {
            player.allowFlying(true);
            abilityHandler.get(player.player()).put(GameAbility.DOUBLE_JUMP, amount);
            addDoubleJumpItems(player, true);
        }
    }

    @Override public final int getCountdown() {
        return countdown.get();
    }

    @Override public final long started() {
        return started;
    }

    @Override public final int getTimeLeft() {
        return timeLeft.get();
    }

    @Override public final void setCountdown(int countdown) {
        this.countdown.set(countdown);
    }

    public final void addDoubleJumpItems(@NotNull MatchPlayer player, boolean newState) {
        DoubleJumpOptions doubleJumpSettings = extension.getDoubleJump();
        if (!doubleJumpSettings.isEnabled() || player.isSpectating()) return;
        DoubleJumpItems items = doubleJumpSettings.getDoubleJumpItems();
        if (items.isEnabled() && doubleJumpSettings.getDefaultAmount() > 0)
            (newState ? items.getAvailable() : items.getUnavailable()).give(items.getSlot(), player.player());
    }

    public final void setExtension(@NotNull MatchExtension extension) {
        this.extension = extension;
    }

    @Override public Map<Integer, PowerupLifecycle> getActivePowerUps() {
        return activePowerUps;
    }

    //<editor-fold desc="Callbacks" defaultstate="collapsed">

    protected void onPlayerJoin(MatchPlayer player) {
        extension.getQuitItem().give(player.player());
        player.gamemode(extension.getWaitingMode());
        for (Entry<Integer, CommandItem> item : extension.getWaitingCmdItems().entrySet()) {
            player.item(item.getKey(), item.getValue().withPlaceholders(player, new StatsEntry(player.getProfile().getExtensionStatistics(extension))));
        }
    }

    protected CompletableFuture<Void> prepare0(MatchPlayer player) {
        return SpleefX.nextTick(() -> player.gamemode(extension.getWaitingMode()));
    }

    protected CompletableFuture<Void> prepareForGame0(MatchPlayer player) {
        return SpleefX.nextTick(() -> player.gamemode(extension.getIngameMode()));
    }

    protected void onPlayerLeave(MatchPlayer player, boolean disconnect) {
    }

    protected void onPlayerEliminate(MatchPlayer player) {
    }

    protected void onPlayerWin(MatchPlayer winner) {
    }

    protected void onContextLoad(MatchPlayer player) {
    }

    protected void onGameStart() {

    }

    protected void onGameStart(@NotNull MatchPlayer player) {
    }

    protected void onGamePlayerLoop(MatchPlayer player) {
    }

    protected void onGameLoop() {
    }

    protected void onCountdownChange(int newCountdown) {
    }

    protected void onPreEnd(boolean force) {
    }

    protected void onPostEnd(boolean force) {
    }

    protected void onGracePeriodStart(MatchPlayer player, boolean real) {
        if (real) {
            Message.GRACE_PERIOD_START.reply(player, extension, new FancyTimeEntry("time", extension.getGracePeriod().getTime() * 100L));
            gracePeriod.set(true);
        }
    }

    protected void onGracePeriodEnd(MatchPlayer player, boolean real) {
        for (Entry<Integer, Item> item : extension.getItemsToAdd().entrySet()) {
            player.item(item.getKey(), item.getValue().respectTeam(player, player, new StatsEntry(player.getProfile().getExtensionStatistics(extension))));
        }
        for (Entry<Integer, CommandItem> item : extension.getIngameCmdItems().entrySet()) {
            player.item(item.getKey(), item.getValue().respectTeam(player, player, new StatsEntry(player.getProfile().getExtensionStatistics(extension))));
        }
        for (Entry<ArmorSlot, Item> armor : extension.getArmorToAdd().entrySet())
            armor.getKey().set(player.player(), armor.getValue().respectTeam(player, player, new StatsEntry(player.getProfile().getExtensionStatistics(extension))));
        if (real) {
            Message.GRACE_PERIOD_ENDED.reply(player, extension, new FancyTimeEntry("time", extension.getGracePeriod().getTime() * 100L));
            gracePeriod.set(false);
        }
    }
    //</editor-fold>

    @NotNull @Override public Iterator<MatchPlayer> iterator() {
        return Iterators.concat(players.iterator(), spectators.iterator());
    }

    @NotNull public Iterable<MatchPlayer> total() {
        return total;
    }

    private class Total implements Iterable<MatchPlayer> {

        @NotNull @Override public Iterator<MatchPlayer> iterator() {
            return Iterators.concat(players.iterator(), spectators.iterator(), extras.iterator());
        }
    }

    protected ReloadedArenaEngine getEngine() {
        return arena.getEngine();
    }

    protected abstract boolean isSingleUnitAlive();

}