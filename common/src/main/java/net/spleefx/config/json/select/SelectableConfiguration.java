/*
 * * Copyright 2020 github.com/moltenjson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.config.json.select;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.spleefx.config.json.MappedConfiguration;
import net.spleefx.json.SpleefXGson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a configuration which saves fields annotated with {@link ConfigOpt} to its configuration.
 * On application bootstrap, registered fields will have their value associated to their JSON keys, or
 * the current field value if the JSON does not map the value yet.
 *
 * @see ConfigOpt
 */
public class SelectableConfiguration {

    private MappedConfiguration config;
    private final File file;

    /**
     * A map which links the class with all its annotated fields
     */
    private final Map<Class<?>, List<Field>> opted = new HashMap<>();

    /**
     * The GSON profile to use when writing
     */
    final Gson gson = SpleefXGson.MAIN;

    public SelectableConfiguration(File file) {
        this.file = file;
        config = new MappedConfiguration(file);
    }

    public SelectableConfiguration register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            List<Field> fields = opt(clazz);
            if (fields.isEmpty()) return this;
            opted.putIfAbsent(clazz, fields);
        }
        return this;
    }

    public SelectableConfiguration associate() {
        opted.forEach((clazz, fields) -> fields.forEach(this::assign));
        return this;
    }

    public SelectableConfiguration reload() {
        config = new MappedConfiguration(file);
        associate();
        return this;
    }

    /**
     * Removes the given key from the JSON file.
     * <p>
     * This will have no effect if the given key does not exist.
     *
     * @param key Key to remove
     */
    public void remove(String key) {
        config.getData().remove(key);
    }

    public void save() {
        opted.forEach((clazz, fields) -> fields.forEach(field -> {
            field.setAccessible(true);
            config.getData().add(getKey(field), gson.toJsonTree(Reflector.getStaticValue(field)));
        }));
        config.save();
    }

    /**
     * Opts all fields from the given class which are annotated with {@link ConfigOpt}, or returns
     * an empty {@link Collection} if the class does not channelTo any fields annotated with it.
     *
     * @param clazz Class to opt from
     * @return A {@link List} of fields from the class which are annotated with {@link ConfigOpt}.
     */
    private List<Field> opt(Class<?> clazz) {
        if (Arrays.stream(clazz.getDeclaredFields()).noneMatch(f -> f.isAnnotationPresent(ConfigOpt.class) && Modifier.isStatic(f.getModifiers())))
            return Collections.emptyList();
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigOpt.class))
                .collect(Collectors.toList());
    }

    /**
     * Returns the key of the field. If the field's {@code ConfigOpt} is empty it will return the field
     * name, otherwise it would return the parameter of {@link ConfigOpt}.
     *
     * @param field Field to fetch from
     * @return The field key
     */
    final String getKey(Field field) {
        if (!field.isAnnotationPresent(ConfigOpt.class))
            throw new RuntimeException("Found a registered key which is not annotated with @ConfigOpt! " + field.getDeclaringClass()
                    + "#" + field.getName());
        ConfigOpt select = field.getAnnotation(ConfigOpt.class);
        return select.value().isEmpty() ? field.getName() : select.value();
    }

    private void assign(Field field) {
        String key = getKey(field);
        if (!config.getData().has(key)) {
            config.getData().add(key, gson.toJsonTree(Reflector.getStaticValue(field)));
            return;
        }
        Object value = Reflector.getValue(this, field);
        Reflector.setStatic(field, value);
        config.getData().add(key, gson.toJsonTree(value));
    }

    /**
     * Returns the content of the configuration. This can be modified
     *
     * @return The configuration content
     */
    public JsonObject getContent() {
        return config.getData();
    }

    public File getFile() {
        return file;
    }

    /**
     * Returns a new {@link SelectableConfiguration} and throws unchecked exceptions if there were any IO exceptions
     *
     * @param file File to use
     * @return The SelectableConfiguration object
     */
    @SneakyThrows public static SelectableConfiguration of(File file) {
        return new SelectableConfiguration(file);
    }
}
