package net.spleefx.core.command;

import org.apache.commons.lang.ArrayUtils;

import java.util.function.IntPredicate;

/**
 * A convenience class to allow anything that extends it to automatically inherit all predicates.
 */
public abstract class ArgumentPredicates {

    private static final IntPredicate NONE = v -> true;
    private static final IntPredicate ZERO = equalTo(0);

    /* predicates */

    public static IntPredicate anything() {
        return NONE;
    }

    public static IntPredicate between(int first, int end) {
        return v -> v >= first && v <= end;
    }

    public static IntPredicate notBetween(int first, int end) {
        return between(first, end).negate();
    }

    public static IntPredicate not(int value) {
        return v -> v != value;
    }

    public static IntPredicate moreThan(int value) {
        return v -> v > value;
    }

    public static IntPredicate lessThan(int value) {
        return v -> v < value;
    }

    public static IntPredicate atLeast(int value) {
        return v -> v >= value;
    }

    public static IntPredicate either(int... values) {
        return v -> ArrayUtils.contains(values, v);
    }

    public static IntPredicate zero() {
        return ZERO;
    }

    public static IntPredicate equalTo(int value) {
        return v -> v == value;
    }

}
