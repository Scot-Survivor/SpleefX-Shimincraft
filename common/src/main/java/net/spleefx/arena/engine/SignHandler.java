package net.spleefx.arena.engine;

import net.spleefx.arena.MatchArena;
import net.spleefx.model.Position;
import net.spleefx.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Iterator;
import java.util.List;

public class SignHandler {

    private final MatchArena arena;

    public SignHandler(MatchArena arena) {
        this.arena = arena;
    }

    public void updateSign(SignChangeEvent sign) {
        List<String> signs = arena.getExtension().getSigns();
        for (int i = 0; i < signs.size(); i++) {
            String line = Placeholders.on(signs.get(i), arena);
            sign.setLine(i, line);
        }
    }

    public void update() {
        if (!Bukkit.isPrimaryThread()) return;
        arena.getEngine().getStage();
        for (Iterator<Position> iterator = arena.getSigns().iterator(); iterator.hasNext(); ) {
            Position loc = iterator.next();
            try {
                updateSign(loc);
            } catch (ClassCastException e) { // The sign no longer exists
                iterator.remove();
            }
        }
    }

    public void updateSign(Position loc) {
        Sign sign = loc.getState();
        List<String> signs = arena.getExtension().getSigns();
        for (int i = 0; i < signs.size(); i++) {
            String line = Placeholders.on(signs.get(i), arena);
            sign.setLine(i, line);
        }
        sign.update();
    }
}
