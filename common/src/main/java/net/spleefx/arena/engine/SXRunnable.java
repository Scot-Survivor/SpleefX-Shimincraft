package net.spleefx.arena.engine;

import lombok.SneakyThrows;
import net.spleefx.SpleefX;
import net.spleefx.util.BukkitSchedulers;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class SXRunnable extends BukkitRunnable {

    protected final CompletableFuture<Void> future = new CompletableFuture<>();

    public SXRunnable() {
        super();
    }

    public <U> CompletableFuture<U> thenApply(Function<? super Void, ? extends U> fn) {
        return future.thenApply(fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super Void, ? extends U> fn) {
        return future.thenApplyAsync(fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super Void, ? extends U> fn, Executor executor) {
        return future.thenApplyAsync(fn, executor);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super Void> action) {
        return future.thenAccept(action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super Void> action) {
        return future.thenAcceptAsync(action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super Void> action, Executor executor) {
        return future.thenAcceptAsync(action, executor);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return future.thenRun(action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return future.thenRunAsync(action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return future.thenRunAsync(action, executor);
    }

    public boolean isRunning() {
        return BukkitSchedulers.getPeriod(BukkitSchedulers.getTask(getTaskId())) != -2;
    }

    protected void complete() {
        cancel();
        future.complete(null);
    }

    protected static JavaPlugin getPlugin() {
        return SpleefX.getPlugin();
    }

    public static boolean isRunning(@Nullable SXRunnable runnable) {
        return runnable != null && runnable.isRunning();
    }

    public static boolean isRunning(@Nullable BukkitTask task) {
        return task != null && getPeriod(task) != -2;
    }

    @SneakyThrows private static long getPeriod(@NotNull BukkitTask task) {
        return (long) PERIOD.invokeWithArguments(task);
    }

    private static final MethodHandle PERIOD;

    static {
        MethodHandle period = null;
        try {
            Field periodField = Protocol.getCraftBukkitClass("scheduler.CraftTask")
                    .getDeclaredField("period");
            if (!periodField.isAccessible()) {periodField.setAccessible(true);}
            period = MethodHandles.lookup().unreflectGetter(periodField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        PERIOD = period;
    }

}
