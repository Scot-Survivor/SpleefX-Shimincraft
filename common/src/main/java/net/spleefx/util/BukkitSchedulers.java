package net.spleefx.util;

import lombok.SneakyThrows;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

public final class BukkitSchedulers {

    private BukkitSchedulers() {}

    @SneakyThrows
    public static long getPeriod(@Nullable BukkitTask task) {
        if (task == null) return -2;
        return (long) PERIOD.invokeWithArguments(task);
    }

    @SneakyThrows
    public static BukkitTask getTask(int id) {
        return RUNNERS.get(id);
    }

    private static final MethodHandle PERIOD;
    private static Map<Integer, BukkitTask> RUNNERS;

    static {
        MethodHandle period = null, runners;
        try {
            Field periodField = Protocol.getCraftBukkitClass("scheduler.CraftTask")
                    .getDeclaredField("period");
            if (!periodField.isAccessible()) {periodField.setAccessible(true);}
            period = MethodHandles.lookup().unreflectGetter(periodField);

            Field runnersField = Bukkit.getScheduler().getClass().getDeclaredField("runners");
            if (!runnersField.isAccessible()) runnersField.setAccessible(true);
            runners = MethodHandles.lookup().unreflectGetter(runnersField).bindTo(Bukkit.getScheduler());
            RUNNERS = (Map<Integer, BukkitTask>) runners.invokeWithArguments();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        PERIOD = period;
    }

}
