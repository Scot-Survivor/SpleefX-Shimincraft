package net.spleefx.team;//package net.spleefx.team;
//
//import net.spleefx.arena.engine.ReloadedArenaEngine;
//import net.spleefx.arena.player.MatchPlayer;
//import net.spleefx.arena.player.PlayerState;
//import net.spleefx.compatibility.PluginCompatibility;
//import net.spleefx.util.item.ItemFactory;
//import net.spleefx.util.menu.Button;
//import net.spleefx.util.message.message.Message;
//import org.bukkit.Material;
//import org.bukkit.Sound;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;
//
//public class TeamButton extends Button {
//
//    public static final ItemStack TEAM_SELECTION_ITEM = ItemFactory.create(Material.JUKEBOX).setName("&bSelect Team").create();
//
//    private static final Sound ON_SELECT = PluginCompatibility.attempt(() -> Sound.valueOf("ENTITY_ARROW_HIT_PLAYER"),
//            () -> Sound.valueOf("SUCCESSFUL_HIT"));
//
//    public TeamButton(Player clicker, int slot, ArenaTeam team, ReloadedArenaEngine arena) {
//        super(slot, createItem(clicker, team, arena));
//        addAction(CANCEL_ACTION).addAction(e -> {
//            MatchPlayer player = MatchPlayer.wrap((Player) e.getWhoClicked());
//            if (team.getMembers().contains(player.player())) {
//                player.msg(Message.ALREADY_IN_THIS_TEAM, team, arena.arena);
//            } else if (team.getMembers().size() == arena.arena.getMembersPerTeam()) {
//                player.msg(Message.TEAM_FULL, team, arena.arena);
//            } else {
//                ArenaTeam oldTeam = arena.getPlayerTeams().get(player);
//                if (oldTeam != null) {
//                    oldTeam.getMembers().remove(player.player());
//                }
//                team.getMembers().add(player.player());
//                arena.getPlayerTeams().put(player, team);
//                player.sound(ON_SELECT);
//                player.contextMessage("&aYou joined " + team.getColor().chat() + "&7.");
//                arena.viewingGUI.forEach((pl, gui) -> gui.init(pl));
//                arena.displayScoreboard(player);
//            }
//        });
//    }
//
//    private static ItemStack createItem(Player player, ArenaTeam team, ReloadedArenaEngine engine) {
//        ItemFactory factory = ItemFactory.create(new ItemStack(team.getColor().getGuiItem()));
//        if (!team.getMembers().isEmpty()) {
//            factory.addLoreLine("");
//            for (Player member : team.getMembers()) {
//                factory.addLoreLine("&b" + member.getName());
//            }
//        }
//        factory.addGlowEffect(team.getMembers().contains(player));
//        return factory.create();
//    }
//
//    public static class InteractionListener implements Listener {
//
//        @EventHandler
//        public void onPlayerInteract(PlayerInteractEvent event) {
//            if (event.getItem() == null) return;
//            MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
//            if (player.getState() != PlayerState.WAITING) return;
//            if (event.getItem().isSimilar(TeamButton.TEAM_SELECTION_ITEM)) {
//                player.gui(new TeamSelectionGUI(player.getArena(), player));
//            }
//        }
//    }
//
//}