package net.spleefx.hook.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.spleefx.SpleefX;
import net.spleefx.hook.luckperms.context.ArenaContextCalculator;
import net.spleefx.hook.luckperms.context.PlayerStateContextCalculator;

public class LuckPermsHook {

    public static void register(SpleefX plugin) {
        LuckPerms api = LuckPermsProvider.get();
        api.getContextManager().registerCalculator(new ArenaContextCalculator());
        api.getContextManager().registerCalculator(new PlayerStateContextCalculator());
    }

}
