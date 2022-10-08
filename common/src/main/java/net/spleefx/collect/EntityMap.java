package net.spleefx.collect;

import com.google.common.collect.ImmutableMap;
import net.spleefx.compatibility.PluginCompatibility;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityMap<E extends Entity, V> implements Map<E, V>, Iterable<Entry<E, V>> {

    private final Map<UUID, V> delegate;

    public EntityMap(@NotNull Map<UUID, V> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is null!");
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(asUUID(key));
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public V get(Object key) {
        return delegate.get(asUUID(key));
    }

    @Nullable @Override public V put(E key, V value) {
        return delegate.put(key.getUniqueId(), value);
    }

    public V remove(Object key) {
        return delegate.remove(asUUID(key));
    }

    @Override public void putAll(@NotNull Map<? extends E, ? extends V> m) {
        delegate.putAll(m.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getUniqueId(), Entry::getValue)));
    }

    public void clear() {
        delegate.clear();
    }

    @NotNull @Override public Set<E> keySet() {
        Set<E> set = new LinkedHashSet<>();
        for (UUID key : delegate.keySet()) {
            set.add((E) PluginCompatibility.getEntity(key));
        }
        return set;
    }

    @NotNull public Collection<V> values() {
        return delegate.values();
    }

    @NotNull @Override public Set<Entry<E, V>> entrySet() {
        Set<Entry<E, V>> set = new LinkedHashSet<>();
        for (Entry<UUID, V> entry : delegate.entrySet()) {
            set.add(new SimpleEntry<>((E) PluginCompatibility.getEntity(entry.getKey()), entry.getValue()));
        }
        return set;
    }

    public Set<Entry<UUID, V>> actualEntrySet() {
        return delegate.entrySet();
    }

    public Set<UUID> actualKeySet() {
        return delegate.keySet();
    }

    private Object asUUID(Object o) {
        if (o instanceof Entity)
            return ((Entity) o).getUniqueId();
        return o;
    }

    public Map<UUID, V> delegate() {
        return delegate;
    }

    public static <E extends Entity, V> EntityMap<E, V> hashMap() {
        return new EntityMap<>(new HashMap<>());
    }

    public static <E extends Entity, V> EntityMap<E, V> linkedHashMap() {
        return new EntityMap<>(new LinkedHashMap<>());
    }

    public static <E extends Entity, V> EntityMap<E, V> concurrentHashMap() {
        return new EntityMap<>(new ConcurrentHashMap<>());
    }

    public static <E extends Entity, V> EntityMap<E, V> immutable(Map<UUID, V> map) {
        return new EntityMap<>(ImmutableMap.copyOf(map));
    }

    public EntityMap<E, V> immutable() {
        return new EntityMap<>(ImmutableMap.copyOf(delegate));
    }

    @NotNull @Override public Iterator<Entry<E, V>> iterator() {
        return entrySet().iterator();
    }
}
