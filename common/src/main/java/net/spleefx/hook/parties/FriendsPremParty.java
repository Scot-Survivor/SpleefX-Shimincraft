//package net.spleefx.hook.parties;
//
//import de.HyChrod.Party.Caching.Partydata.Member;
//import de.HyChrod.Party.Utilities.PartyAPI;
//import net.spleefx.arena.player.MatchPlayer;
//import org.bukkit.Bukkit;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class FriendsPremParty implements PartyHook {
//
//    @Override public @NotNull SXParty getParty(@NotNull MatchPlayer player) {
//        List<Member> players = PartyAPI.getMembers(player.uuid());
//        if (players == null || players.isEmpty()) return SXParty.NONE;
//        return new SXParty(
//                map(players.stream()),
//                map(players.stream().filter(member -> member.getLeader() == 1))
//        );
//    }
//
//    private static List<MatchPlayer> map(@NotNull Stream<Member> members) {
//        return members.map(Member::getUuid).map(Bukkit::getPlayer).map(MatchPlayer::wrap).collect(Collectors.toList());
//    }
//
//}
