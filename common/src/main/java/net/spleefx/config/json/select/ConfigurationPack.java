/*
 * * Copyright 2020 github.com/ReflxctionDev
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

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.spleefx.config.json.MappedConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationPack {

    private final File directory;

    private final Gson gson;

    private final Object instance;

    private final Map<Field, MappedConfiguration> fieldMap = new HashMap<>();

    public ConfigurationPack(Object instance, File directory, Gson gson) {
        this.instance = instance;
        this.directory = directory;
        this.gson = gson;
    }

    private MappedConfiguration getFieldFile(Field field) {
        Preconditions.checkArgument(field.isAnnotationPresent(ConfigOpt.class), "Field " + field.getName() + " is not annotated with @ConfigOpt!");
        ConfigOpt derive = field.getAnnotation(ConfigOpt.class);
        return new MappedConfiguration(new File(directory, derive.value().replace('/', File.separatorChar)));
    }

    public void updateField(Field field) throws IOException {
        MappedConfiguration file = fieldMap.computeIfAbsent(field, this::getFieldFile);
        field.setAccessible(true);
        JsonObject content = file.getData();
        if ((!content.entrySet().isEmpty())) {
            try {
                field.set(instance, gson.fromJson(content, field.getGenericType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Attempted to set non-static field " + field.getName() + " from a class instance.");
            }
        }
    }

    public void updateField(String fieldName) throws IOException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            updateField(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void saveField(Field field) throws IOException {
        MappedConfiguration file = fieldMap.computeIfAbsent(field, this::getFieldFile);
        field.setAccessible(true);
        try {
            file.setData((JsonObject) gson.toJsonTree(field.get(instance)));
            file.save();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void saveField(String fieldName) throws IOException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            saveField(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public ConfigurationPack register() throws IOException {
        for (Field field : selectFields()) {
            updateField(field);
        }
        return this;
    }

    public void save() throws IOException {
        for (Field field : selectFields()) {
            saveField(field);
        }
    }

    public ConfigurationPack refresh() {
        for (Field field : selectFields()) {
            try {
                updateField(field);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    private List<Field> selectFields() {
        if (instance instanceof Class)
            return Arrays.stream(((Class<?>) instance).getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ConfigOpt.class)).collect(Collectors.toList());
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConfigOpt.class)).collect(Collectors.toList());
    }

}