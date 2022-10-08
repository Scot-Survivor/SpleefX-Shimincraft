package net.spleefx.team;//package net.spleefx.team;
//
//import net.spleefx.arena.engine.ReloadedArenaEngine;
//import net.spleefx.arena.MatchArena;
//import net.spleefx.arena.player.MatchPlayer;
//import net.spleefx.util.menu.GameMenu;
//import org.bukkit.entity.HumanEntity;
//import org.bukkit.entity.Player;
//
//import java.util.List;
//
//public class TeamSelectionGUI extends GameMenu {
//
//    private final MatchArena arena;
//    private final MatchPlayer player;
//
//    public TeamSelectionGUI(MatchArena arena, MatchPlayer player) {
//        super("&5Select Team", getAppropriateSize(arena.getTeams().size()));
//        this.arena = arena;
//        this.player = player;
//        init(player);
//    }
//
//    public void init(MatchPlayer player) {
//        List<ArenaTeam> matchTeams = arena.getArenaTeams();
//        for (int i = 0; i < matchTeams.size(); i++) {
//            ArenaTeam team = matchTeams.get(i);
//            setButton(new TeamButton(player.player(), i, team, (ReloadedArenaEngine) arena.getEngine()));
//        }
//
//        closeActions.add((e) -> ((ReloadedArenaEngine) arena.getEngine()).viewingGUI.remove(MatchPlayer.wrap((Player) e.getPlayer())));
//        display(player.player());
//    }
//
//    @Override public void display(HumanEntity entity) {
//        super.display(entity);
//        ((ReloadedArenaEngine) arena.getEngine()).viewingGUI.put(player, this);
//    }
//}