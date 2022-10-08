package net.spleefx.backend;

import net.spleefx.SpleefX;
import net.spleefx.arena.engine.AsyncDelay;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class Schedulers {

    public static final ScheduledExecutorService SCHEDULED_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final AsyncDelay<DelayContext> DELAY = new AsyncDelay<>();
    public static final ForkJoinPool POOL = new ForkJoinPool();

    public static CompletableFuture<Void> wait(long time, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SCHEDULED_SERVICE.schedule(() -> {
            Bukkit.getScheduler().runTask(SpleefX.getPlugin(), () -> future.complete(null));
        }, time, unit);
        return future;
    }

    public static RuntimeException sneakyThrow(@NotNull Throwable t) {
        return sneakyThrow0(t);
    }

    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

}
