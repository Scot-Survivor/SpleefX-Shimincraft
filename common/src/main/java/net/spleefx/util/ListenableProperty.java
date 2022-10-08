package net.spleefx.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A thread-safe implementation for listening to changes on a property
 *
 * @param <T> Property type.
 */
public interface ListenableProperty<T> {

    T get();

    T to(T value); // get and set

    T set(T value);

    static <T> ListenableProperty<T> of(T value, Consumer<T> change) {
        return of(value, (t, t2) -> change.accept(t2));
    }

    static <T> ListenableProperty<T> of(Consumer<T> change) {
        return of(null, change);
    }

    static <T> ListenableProperty<T> of(T value, Runnable change) {
        return of(value, (t, t2) -> change.run());
    }

    static <T> ListenableProperty<T> of(Runnable change) {
        return of(null, change);
    }

    static <T> ListenableProperty<T> of(BiConsumer<T, T> change) {
        return of(null, change);
    }

    static <T> ListenableProperty<T> of(T value, BiConsumer<T, T> change) {
        AtomicReference<T> ref = new AtomicReference<>(value);
        return new ListenableProperty<T>() {
            @Override public T get() {
                return ref.get();
            }

            @Override public T to(T value) {
                T o = ref.getAndSet(value);
                if (value != o) change.accept(o, value);
                return o;
            }

            @Override public T set(T value) {
                T o = ref.getAndSet(value);
                if (value != o) change.accept(o, value);
                return value;
            }
        };
    }

}
