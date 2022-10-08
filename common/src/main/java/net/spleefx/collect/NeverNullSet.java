package net.spleefx.collect;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonAdapter(NeverNullTypeAdapterFactory.class)
public class NeverNullSet<T> extends ForwardingSet<T> implements NeverNullCollection {

    private final Set<T> delegate;

    public NeverNullSet(Set<T> delegate) {
        if (delegate instanceof ImmutableSet)
            this.delegate = delegate.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        else {
            delegate.removeIf(Objects::isNull);
            this.delegate = delegate;
        }
    }

    @Override public boolean add(@Nullable T element) {
        if (element != null)
            return super.add(element);
        return false;
    }

    @Override public boolean addAll(@NotNull Collection<? extends T> collection) {
        return super.standardAddAll(collection);
    }

    @Override protected Set<T> delegate() {
        return delegate;
    }
}
