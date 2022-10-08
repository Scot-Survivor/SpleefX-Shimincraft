package net.spleefx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public static <T> void merge(T from, T to) {
        for (Field f : getAllFields(from.getClass())) {
            try {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(to, f.get(from));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static Method method(Class<?> c, String name, Class<?>... parameters) {
        try {
            return c.getDeclaredMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
