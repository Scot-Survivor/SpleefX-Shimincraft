package net.spleefx.collect;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonAdapter(NeverNullTypeAdapterFactory.class)
public class NeverNullList<T> extends ForwardingList<T> implements NeverNullCollection {

    private final List<T> delegate;

    public NeverNullList(List<T> delegate) {
        if (delegate instanceof ImmutableList)
            this.delegate = delegate.stream().filter(Objects::nonNull).collect(Collectors.toList());
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

    @Override public boolean addAll(int index, @NotNull Collection<? extends T> elements) {
        return super.standardAddAll(index, elements);
    }

    @Override public void add(int index, @Nullable T element) {
        if (element != null)
            super.add(index, element);
    }

    @Override protected List<T> delegate() {
        return delegate;
    }
}
