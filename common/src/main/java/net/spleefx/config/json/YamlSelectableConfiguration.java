package net.spleefx.config.json;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlSelectableConfiguration {

    /**
     * A map which links the class with all its annotated fields
     */
    private final Map<Class<?>, List<Field>> opted = new HashMap<>();

    public void sync(Class<?> c) {

    }

}