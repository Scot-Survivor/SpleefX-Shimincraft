package net.spleefx.arena;

import com.google.common.collect.ImmutableMap.Builder;
import net.spleefx.SpleefX;
import net.spleefx.arena.type.bowspleef.BowSpleefArena;
import net.spleefx.arena.type.custom.ExtensionArena;
import net.spleefx.arena.type.spleef.SpleefArena;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.config.json.select.ConfigOpt;
import net.spleefx.config.json.select.SelectionEntry;
import net.spleefx.extension.MatchExtension;
import net.spleefx.hook.worldedit.NoSchematicException;
import net.spleefx.hook.worldedit.SchematicManager;
import net.spleefx.model.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Arenas {

    private static final ArenaFactory<?> DEFAULT = ExtensionArena::new;
    private static final Comparator<MatchArena> BY_COUNT = (a, b) -> Integer.compare(b.getEngine().getPlayers().size(), a.getEngine().getPlayers().size());
    private static final Predicate<MatchArena> ALLOWED_TO_JOIN = arena -> !arena.getEngine().isFull() && arena.getEngine().getStage().isPlayable();

    private static final Map<String, ArenaFactory<?>> FACTORIES = new Builder<String, ArenaFactory<?>>()
            .put("spleef", SpleefArena::new)
            .put("splegg", SpleggArena::new)
            .put("bow_spleef", BowSpleefArena::new)
            .build();

    @ConfigOpt("arenas")
    private static final SelectionEntry<Map<String, MatchArena>> ARENAS = new SelectionEntry<>(new HashMap<>());

    public static <A extends MatchArena> A createArena(
            @NotNull String key,
            @NotNull String displayName,
            @NotNull Position origin,
            @NotNull ArenaType arenaType,
            @NotNull MatchExtension extension
    ) {
        A arena = (A) getFactory(extension).create(key, displayName, origin, arenaType, extension);
        ARENAS.get().put(arena.key, arena);
        return arena;
    }

    public static <A extends MatchArena> A createArena(
            @NotNull String key,
            @NotNull Position origin,
            @NotNull ArenaType arenaType,
            @NotNull MatchExtension extension
    ) {
        return createArena(key, key, origin, arenaType, extension);
    }

    public static CompletableFuture<Void> regenerateArena(@NotNull MatchArena arena) {
        SchematicManager sm = SpleefX.newSchematicManager(arena.getKey());
        try {
            return sm.paste(arena.getOrigin().asLocation());
        } catch (NoSchematicException e) {
            throw new IllegalStateException("Schematic /SpleefX/arenas/" + arena.getKey() + ".schem no longer exists!");
        }
    }

    public static MatchArena deleteArena(@NotNull String arena) {
        @Nullable MatchArena removed = ARENAS.get().remove(arena);
        if (removed != null)
            new File(SpleefX.getSpleefX().getArenasFolder(), arena + ".schem").delete();
        return removed;
    }

    public static void registerAll(@Nullable Map<String, MatchArena> map) {
        if (map != null)
            ARENAS.get().putAll(map);
    }

    public static int size() {
        return ARENAS.get().size();
    }

    public static Map<String, MatchArena> getArenas() {
        return ARENAS.get();
    }

    @Nullable
    public static MatchArena find(@NotNull Predicate<MatchArena> test) {
        for (MatchArena arena : ARENAS.get().values()) {
            if (test.test(arena))
                return arena;
        }
        return null;
    }

    @NotNull
    public static MatchArena deleteArena(@NotNull MatchArena arena) {
        return deleteArena(arena.key);
    }

    public static <A extends MatchArena> ArenaFactory<A> getFactory(@NotNull MatchExtension extension) {
        return (ArenaFactory<A>) FACTORIES.getOrDefault(extension.getKey(), DEFAULT);
    }

    @FunctionalInterface
    private interface ArenaFactory<A extends MatchArena> {

        A create(String key, String displayName, Position regenerationPoint, ArenaType arenaType, MatchExtension extension);
    }

    public static List<MatchArena> getByExtension(MatchExtension extension) {
        return ARENAS.get().values().stream().filter(t -> t.extension.equals(extension))
                .collect(Collectors.toList());
    }

    public static <R extends MatchArena> R pick(@NotNull MatchExtension mode) {
        return (R) Arenas.getArenas()
                .values()
                .stream()
                .filter(a -> a.getExtension().getKey().equals(mode.getKey()) && ALLOWED_TO_JOIN.test(a))
                .min(BY_COUNT)
                .orElse(null);
    }

    public static <R extends MatchArena> R pick() {
        return (R) Arenas.getArenas()
                .values()
                .stream()
                .min(BY_COUNT)
                .orElse(null);
    }
}
