package net.spleefx.event.listen;

import net.spleefx.SpleefX;
import net.spleefx.event.SpleefXEvent;
import net.spleefx.event.ability.PlayerDoubleJumpEvent;
import net.spleefx.event.arena.ArenaRegenerateEvent;
import net.spleefx.event.arena.TeamLoseEvent;
import net.spleefx.event.arena.TeamWinEvent;
import net.spleefx.event.arena.end.PostArenaEndEvent;
import net.spleefx.event.arena.end.PreArenaEndEvent;
import net.spleefx.event.player.PlayerDestroyBlockInArenaEvent;
import net.spleefx.event.player.PlayerLoseEvent;
import net.spleefx.event.player.PlayerPutInTeamEvent;
import net.spleefx.event.player.PlayerWinGameEvent;
import net.spleefx.spectate.PlayerExitSpectateEvent;
import net.spleefx.spectate.PlayerSpectateAnotherEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * An adapter class to allow listening to each event individually.
 */
public abstract class EventListenerAdapter implements EventListener {

    static final Set<EventListener> LISTENERS = new HashSet<>();

    protected final SpleefX plugin = SpleefX.getSpleefX();

    // @formatter:off
    @Override
    public final void onEvent(@NotNull SpleefXEvent event) {
        if (event instanceof PreArenaEndEvent) onPreArenaEnd((PreArenaEndEvent) event);
        else if (event instanceof PostArenaEndEvent) onPostArenaEnd((PostArenaEndEvent) event);
        else if (event instanceof ArenaRegenerateEvent) onArenaRegenerate((ArenaRegenerateEvent) event);
//        else if (event instanceof ArenaStartedEvent) onArenaStarted((ArenaStartedEvent) event);
        else if (event instanceof PlayerDoubleJumpEvent) onPlayerDoubleJump((PlayerDoubleJumpEvent) event);
        else if (event instanceof PlayerDestroyBlockInArenaEvent) onPlayerDestroyBlockInArena((PlayerDestroyBlockInArenaEvent) event);
        else if (event instanceof PlayerLoseEvent) onPlayerLose((PlayerLoseEvent) event);
        else if (event instanceof PlayerWinGameEvent) onPlayerWinGame((PlayerWinGameEvent) event);
        else if (event instanceof PlayerPutInTeamEvent) onPlayerPutInTeam((PlayerPutInTeamEvent) event);
        else if (event instanceof PlayerSpectateAnotherEvent) onPlayerSpectateAnother((PlayerSpectateAnotherEvent) event);
        else if (event instanceof PlayerExitSpectateEvent) onPlayerExitSpectate((PlayerExitSpectateEvent) event);
        else if (event instanceof TeamWinEvent) onTeamWin((TeamWinEvent) event);
        else if (event instanceof TeamLoseEvent) onTeamLose((TeamLoseEvent) event);
    }

    public void onPreArenaEnd(PreArenaEndEvent event) {}
    public void onPostArenaEnd(PostArenaEndEvent event) {}
    public void onArenaRegenerate(ArenaRegenerateEvent event) {}
//    public void onArenaStarted(ArenaStartedEvent event) {}

    public void onPlayerDoubleJump(PlayerDoubleJumpEvent event) {}
    public void onPlayerDestroyBlockInArena(PlayerDestroyBlockInArenaEvent event) {}
    public void onPlayerLose(PlayerLoseEvent event) {}
    public void onPlayerWinGame(PlayerWinGameEvent event) {}
    public void onPlayerSpectateAnother(PlayerSpectateAnotherEvent event) {}
    public void onPlayerExitSpectate(PlayerExitSpectateEvent event) {}
    public void onPlayerPutInTeam(PlayerPutInTeamEvent event) {}

    public void onTeamWin(TeamWinEvent event) {}
    public void onTeamLose(TeamLoseEvent event) {}
    // @formatter:on

}