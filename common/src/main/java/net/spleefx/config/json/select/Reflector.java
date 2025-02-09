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

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A class with different unsafe reflection utilities
 * <p>
 * This class is not intended for public usage.
 */
@SuppressWarnings("unchecked")
class Reflector {

    // Cannot be initiated
    private Reflector() {
        throw new AssertionError(Reflector.class.getName() + " cannot be initiated");
    }

    static void setStatic(Field field, Object value) {
        try {
            field.setAccessible(true);
            if (field.getType().equals(SelectionEntry.class)) {
                getSelectionHolder(field).set(value);
                return;
            }
            field.set(field.getDeclaringClass(), value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static Object getValue(SelectableConfiguration configuration, Field field) {
        field.setAccessible(true);
        Type d = field.getType();
        if (d.equals(SelectionEntry.class)) {
            d = getGeneric(field);
        }
        return configuration.gson.fromJson(configuration.getContent().get(configuration.getKey(field)), d);
    }

    static Object getStaticValue(Field field) {
        try {
            field.setAccessible(true);
            if (field.getType().equals(SelectionEntry.class)) {
                return getSelectionHolder(field).get();
            }
            return field.get(field.getDeclaringClass());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private static SelectionEntry getSelectionHolder(Field field) {
        try {
            field.setAccessible(true);
            return (SelectionEntry) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Type getGeneric(Field field) {
        field.setAccessible(true);
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return TypeToken.of(type).resolveType(SelectionEntry.class.getTypeParameters()[0]).getType();
    }

}
