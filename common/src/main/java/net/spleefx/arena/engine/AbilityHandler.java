package net.spleefx.arena.engine;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import net.spleefx.collect.EntityMap;
import net.spleefx.extension.ability.GameAbility;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AbilityHandler extends ForwardingMap<Player, Map<GameAbility, Integer>> {

    private static final ImmutableMap<GameAbility, Integer> ALL = ImmutableMap.copyOf(Arrays
            .stream(GameAbility.values)
            .collect(Collectors.toMap(i -> i, i -> 0))
    );

    private final EntityMap<Player, Map<GameAbility, Integer>> abilities = EntityMap.hashMap();

    public Map<GameAbility, Integer> get(Player player) {
        return computeIfAbsent(player, p -> new HashMap<>(ALL));
    }

    @SuppressWarnings("ConstantConditions")
    public int consume(Player player, GameAbility ability) {
        return get(player).computeIfPresent(ability, (a, old) -> old - 1);
    }

    public int get(Player player, GameAbility ability) {
        return get(player).getOrDefault(ability, 0);
    }

    @Override protected Map<Player, Map<GameAbility, Integer>> delegate() {
        return abilities;
    }
}
