package net.spleefx.hook.parties;

import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.config.SpleefXConfig;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public interface PartyHook {

    @NotNull SXParty getParty(@NotNull MatchPlayer player);

    static PartyHook findHook() {
//        SpleefXConfig.PARTY_SUPPORT.get();
//        if (Bukkit.getPluginManager().getPlugin("FriendsPremium") != null)
//            return new FriendsPremParty();
        return player -> SXParty.NONE;
    }

    static PartyHook current() {
        return PluginCompatibility.getParties();
    }

}
