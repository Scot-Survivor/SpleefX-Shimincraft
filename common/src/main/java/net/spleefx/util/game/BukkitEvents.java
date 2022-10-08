package net.spleefx.util.game;

import net.spleefx.SpleefX;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.spleefx.util.Util.n;

public final class BukkitEvents {

    private static final Listener EMPTY_LISTENER = new Listener() {};

    private static final Map<Class<?>, HandlerList> handlerLists = new ConcurrentHashMap<>();

    private BukkitEvents() { }

    public static <T extends Event> EventTag<T> nextEvent(@NotNull Class<T> type) {
        return nextEvent(type, e -> true, EventPriority.NORMAL);
    }

    public static <T extends Event> EventTag<T> nextEvent(@NotNull Class<T> type,
                                                          Predicate<T> checks) {
        return nextEvent(type, checks, EventPriority.NORMAL);
    }

    public static <T extends Event> EventTag<T> nextEvent(Class<T> type,
                                                          Predicate<T> checks,
                                                          EventPriority priority) {
        return nextEvent(type, checks, priority, false);
    }

    public static <T extends Event> EventTag<T> nextEvent(Class<T> type,
                                                          Predicate<T> checks,
                                                          EventPriority priority,
                                                          boolean ignoreCancelled) {
        return nextEvents(1, type, checks, priority, ignoreCancelled);
    }

    public static <T extends Event> EventTag<T> nextEvents(int count,
                                                           Class<T> type,
                                                           Predicate<T> checks,
                                                           EventPriority priority,
                                                           boolean ignoreCancelled) {
        AtomicInteger invokeTimes = new AtomicInteger(count);
        HandlerList list = getHandlerList(type);
        EventTag<T> future = new EventTag<>();
        EventExecutor executor = (listener, event) -> {
            if (invokeTimes.getAndDecrement() > 0) {
                future.callbacks.forEach(t -> t.accept((T) event));
            }
        };
        RegisteredListener listener = new RegisteredListener(EMPTY_LISTENER, executor, priority, SpleefX.getPlugin(), ignoreCancelled) {
            public void callEvent(@NotNull Event event) throws EventException {
                if (type.isAssignableFrom(event.getClass())) {
                    if (checks.test((T) event)) {
                        super.callEvent(event);
                        if (invokeTimes.get() <= 0)
                            list.unregister(this);
                    }
                }
            }
        };
        list.register(listener);
        return future;
    }

    public static <T extends Event> EventTag<T> timedEvent(long duration,
                                                           TimeUnit unit,
                                                           Class<T> type,
                                                           Predicate<T> checks,
                                                           EventPriority priority,
                                                           boolean ignoreCancelled) {
        AtomicBoolean finished = new AtomicBoolean();
        HandlerList list = getHandlerList(type);
        EventTag<T> future = new EventTag<>();
        EventExecutor executor = (listener, event) -> {
            if (!finished.get()) {
                future.callbacks.forEach(t -> t.accept((T) event));
            }
        };
        RegisteredListener listener = new RegisteredListener(EMPTY_LISTENER, executor, priority, SpleefX.getPlugin(), ignoreCancelled) {
            public void callEvent(@NotNull Event event) throws EventException {
                if (type.isAssignableFrom(event.getClass())) {
                    if (checks.test((T) event)) {
                        super.callEvent(event);
                        if (finished.get())
                            list.unregister(this);
                    }
                }
            }
        };
        list.register(listener);
        SpleefX.SCHEDULED_SERVICE.schedule(() -> finished.set(true), duration, unit);
        return future;
    }

    private static HandlerList getHandlerList(@NotNull Class<? extends Event> type) {
        return handlerLists.computeIfAbsent(type, cl -> {
            while (cl.getSuperclass() != null && Event.class.isAssignableFrom(cl.getSuperclass())) {
                try {
                    Method method = cl.getDeclaredMethod("getHandlerList");
                    method.setAccessible(true);
                    return (HandlerList) method.invoke(null);
                } catch (NoSuchMethodException var2) {
                    cl = cl.getSuperclass().asSubclass(Event.class);
                } catch (Exception var3) {
                    throw new IllegalPluginAccessException(var3.getMessage());
                }
            }

            throw new IllegalPluginAccessException("Unable to find handler list for event " + cl.getName());
        });
    }

    public static class EventTag<T> {

        private final List<Consumer<T>> callbacks = new ArrayList<>();

        public EventTag<T> thenAccept(@NotNull Consumer<T> callback) {
            callbacks.add(n(callback, "callback"));
            return this;
        }
    }

}
