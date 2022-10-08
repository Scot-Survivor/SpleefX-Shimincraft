package net.spleefx.hook.luckperms.context;

import net.luckperms.api.context.ContextCalculator;
import org.bukkit.entity.Player;

public abstract class SimpleLPContext implements ContextCalculator<Player> {

    protected static final String NAMESPACE = "spleefx";

    protected static final String ARENA = namespace("arena");
    protected static final String STATE = namespace("state");

    protected static String namespace(String key) {
        return NAMESPACE + ":" + key;
    }

}
