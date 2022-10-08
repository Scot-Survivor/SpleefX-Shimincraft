package net.spleefx.event;

import lombok.Getter;
import net.spleefx.SpleefX;

/**
 * Represents a SpleefX-related event
 */
@Getter
public abstract class SpleefXEvent {

    /**
     * Whether was the event cancelled or not. Note that not all events are cancellable.
     *
     * @see #isCancellable() .
     */
    protected boolean cancelled;

    /**
     * Whether can the event be cancelled or not
     */
    protected final boolean cancellable = getClass().isAnnotationPresent(CancellableEvent.class);

    /**
     * Sets whether is the event cancelled or not
     *
     * @param cancelled New value to set
     */
    public void setCancelled(boolean cancelled) {
        if (cancelled && !cancellable) {
            String callerName = Thread.currentThread().getStackTrace()[2].getClassName();
            SpleefX.logger().warning(callerName + " attempted to cancel a non-cancellable event: " + getClass().getSimpleName() + ".");
            return;
        }
        this.cancelled = cancelled;
    }

}