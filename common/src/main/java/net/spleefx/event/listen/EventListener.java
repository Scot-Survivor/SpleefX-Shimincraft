package net.spleefx.event.listen;

import net.spleefx.SpleefX;
import net.spleefx.event.SpleefXEvent;
import org.jetbrains.annotations.NotNull;

import static net.spleefx.event.listen.EventListenerAdapter.LISTENERS;

/**
 * Represents an event listener
 */
public interface EventListener {

    /**
     * Invoked on any event.
     *
     * @param event Event that was triggered
     */
    void onEvent(@NotNull SpleefXEvent event);

    /**
     * Invokes the specified event
     *
     * @param event Event to fire
     */
    static boolean post(SpleefXEvent event) {
        synchronized (LISTENERS) {
            for (EventListener listener : LISTENERS) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    SpleefX.logger().warning("Failed to dispatch event " + event.getClass().getName() + " to listener " + listener + ": ");
                    e.printStackTrace();
                }
            }
            return event.isCancelled();
        }
    }

    /**
     * Registers the specified listener
     *
     * @param listener Listener to register
     */
    static void register(EventListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.add(listener);
        }
    }

}