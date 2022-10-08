package net.spleefx.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static net.spleefx.compatibility.ProtocolNMS.RANDOM;

public class Util {

    public static int coerceAtLeast(int value, int min) {
        return Math.max(value, min);
    }

    public static int coerceAtMost(int value, int max) {
        return Math.min(value, max);
    }

    public static int coerce(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public static <T> T n(T value) {
        return Objects.requireNonNull(value);
    }

    public static <T> T n(T value, String message) {
        return Objects.requireNonNull(value, message);
    }

    public static <T> T random(@NotNull List<T> t) {
        Preconditions.checkArgument(!t.isEmpty(), "List is empty!");
        return t.get(RANDOM.nextInt(t.size()));
    }

    public static <T> T random(@NotNull T[] t) {
        Preconditions.checkArgument(t.length > 0, "Array is empty!");
        return t[RANDOM.nextInt(t.length)];
    }

}
