package net.spleefx.arena.engine;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import net.spleefx.SpleefX;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.team.ArenaTeam;
import net.spleefx.arena.team.ArenaTeam.ImmutableArenaTeam;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.event.arena.TeamLoseEvent;
import net.spleefx.event.arena.TeamWinEvent;
import net.spleefx.event.listen.EventListener;
import net.spleefx.event.player.PlayerPutInTeamEvent;
import net.spleefx.model.CommandExecution;
import net.spleefx.model.Position;
import net.spleefx.util.message.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static net.spleefx.util.Placeholders.firstNotNull;

public class TeamsArenaEngine extends AbstractArenaEngine {

    protected final List<ImmutableArenaTeam> deadTeams = new ArrayList<>();
    protected final Collection<ArenaTeam> arenaTeams;

    protected final Map<MatchPlayer, ArenaTeam> playerTeams = new HashMap<>();

    public TeamsArenaEngine(@NotNull MatchArena arena) {
        super(arena);
        arenaTeams = Collections2.transform(arena.getTeams(), new TeamMappingFunction());
    }

    @Override protected CompletableFuture<Void> prepare0(MatchPlayer player) {
        ArenaTeam team = pickTeam();
        PlayerPutInTeamEvent event = new PlayerPutInTeamEvent(player.player(), arena, team);
        EventListener.post(event);
        playerTeams.put(player, event.getTeam());
        team.all.add(player);
        Position position = firstNotNull(arena.getTeamLobbies().get(team.team), arena.getLobby(), arena.getSpawnPoints().get(team.team));
        player.teleport(position);
        return super.prepare0(player);
    }

    @Override
    protected void onPlayerJoin(MatchPlayer player) {
        super.onPlayerJoin(player);
        ArenaTeam team = getTeam(player);
        for (MatchPlayer pl : this) {
            Message.PLAYER_JOINED_T.reply(pl, arena, team.team, player, arena.getExtension());
        }
        Position position = firstNotNull(arena.getTeamLobbies().get(team.team), arena.getLobby(), arena.getSpawnPoints().get(team.team));
        player.teleport(position);
    }

    @Override protected void onPlayerEliminate(MatchPlayer player) {
        ArenaTeam team = getTeam(player);
        team.alive.remove(player);
        if (team.alive.size() == 0) {
            teamEliminated(team);
        }
        for (MatchPlayer p : total()) {
            p.msg(Message.PLAYER_LOST_T, arena, team, player);
        }
    }

    @Override
    public void onPlayerLeave(@NotNull MatchPlayer player, boolean disconnect) {
        ArenaTeam team = playerTeams.remove(player);
        team.alive.remove(player);
        team.all.remove(player);
    }

    @Override
    public void onGameStart(@NotNull MatchPlayer player) {
        ArenaTeam team = getTeam(player);
        team.alive.add(player);
        player.teleport(arena.getSpawnPoints().get(team.team));
    }

    @Override protected void onPlayerWin(MatchPlayer winner) {
        ArenaTeam team = getTeam(winner);
        for (MatchPlayer player : total()) {
            player.msg(Message.PLAYER_WINS_T, winner, team.team, arena);
        }
    }

    protected ArenaTeam pickTeam() {
        LinkedList<ArenaTeam> teams = new LinkedList<>(arenaTeams);
        Collections.sort(teams);
        if (playerTeams.isEmpty())
            Collections.shuffle(teams);
        teams.removeIf(ArenaTeam::isFull);
        ArenaTeam picked = teams.peekFirst();
        for (ArenaTeam team : arenaTeams) {
            if (team.equals(picked))
                return team;
        }
        return picked;
//        return arenaTeams.stream().findFirst().get();
    }

    @Override public boolean isReady() {
        //            return ready && arena.getLobby() != null;
        return arena.getTeams().size() < 2 || arena.getSpawnPoints().keySet().containsAll(arena.getTeams());
    }

    @Override public Map<MatchPlayer, ArenaTeam> getTeams() {
        return playerTeams;
    }

    public final void teamEliminated(ArenaTeam team) {
        for (MatchPlayer player : team.all) {
            getStats(player).lose();
        }
        for (MatchPlayer player : this) {
            player.msg(Message.TEAM_ELIMINATED, team.team);
        }
        ImmutableArenaTeam immutableArenaTeam = ImmutableArenaTeam.from(team);
        deadTeams.add(immutableArenaTeam);
        EventListener.post(new TeamLoseEvent(arena, immutableArenaTeam));
    }

    public ArenaTeam getTeam(MatchPlayer player) {
        return playerTeams.get(player);
    }

    @Override public @Nullable Collection<MatchPlayer> checkForWinners() {
        List<ArenaTeam> aliveTeams = arenaTeams.stream().filter(ArenaTeam::isAlive).collect(Collectors.toList());
        if (aliveTeams.size() == 1) {
            ArenaTeam winningTeam = aliveTeams.get(0);
            teamWin(winningTeam);
            return winningTeam.all;
        }
        return super.checkForWinners();
    }

    public void teamWin(ArenaTeam team) {
        ImmutableArenaTeam immutable = ImmutableArenaTeam.from(team);
        deadTeams.add(immutable);
        Collections.reverse(deadTeams);
        EventListener.post(new TeamWinEvent(arena, immutable));
    }

    private void runWinningCommands() {
        List<ImmutableArenaTeam> deadTeams = new ArrayList<>(this.deadTeams);
        SpleefX.nextTick(20, () -> {
            // run reward commands
            for (Entry<Integer, CommandExecution> reward : extension.getRunCommandsForTeamWinners().entrySet()) {
                try {
                    deadTeams.remove(reward.getKey() - 1).all.forEach(t -> reward.getValue().execute(t.player(), arena));
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
            CommandExecution reward = extension.getRunCommandsForTeamWinners().get(-1);
            if (reward != null) {
                deadTeams.forEach(t -> t.all.forEach(p -> reward.execute(p.player(), arena)));
            }
        });
    }

    @Override protected void onPreEnd(boolean force) {
        if (!force)
            runWinningCommands();
    }

    @Override public void onPostEnd(boolean force) {
        deadTeams.clear();
        playerTeams.clear();
        arenaTeams.forEach(ArenaTeam::flush);
    }

    @Override protected void onGracePeriodStart(MatchPlayer player, boolean real) {
        super.onGracePeriodStart(player, real);
    }

    @Override protected void onGracePeriodEnd(MatchPlayer player, boolean real) {
        super.onGracePeriodEnd(player, real);
    }

    public Collection<ArenaTeam> getArenaTeams() {
        return arenaTeams;
    }

    @Override protected boolean isSingleUnitAlive() {
        return arenaTeams.stream().filter(ArenaTeam::isAlive).count() == 1;
    }

    class TeamMappingFunction implements Function<MatchTeam, ArenaTeam> {

        private final Map<MatchTeam, ArenaTeam> teams = new ConcurrentHashMap<>();

        @Override public ArenaTeam apply(MatchTeam team) {
            return teams.computeIfAbsent(team, t -> new ArenaTeam(TeamsArenaEngine.this, t));
        }
    }

}
