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
package net.spleefx.util;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import net.spleefx.json.SimpleAdapter;
import net.spleefx.util.Percentage.Adapter;

import java.lang.reflect.Type;
import java.security.SecureRandom;

/**
 * A class for parsing percentages
 */
@JsonAdapter(Adapter.class)
public class Percentage {

    /**
     * Random used to generate numbers
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * The percentage chance
     */
    @Expose
    private int chance;

    /**
     * Creates a percentage with the specified chance
     *
     * @param chance Chance of this percentage
     */
    public Percentage(int chance) {
        this.chance = chance;
    }

    /**
     * Returns the chance of this percentage
     *
     * @return The chance
     */
    public int chance() {
        return chance;
    }

    /**
     * Generates a random number in this percentage, and tests whether it is their lucky day or not
     *
     * @return ^
     */
    public boolean isApplicable() {
        return generate() <= chance;
    }

    /**
     * Generates a number between this percentage
     *
     * @return The newly generated number
     */
    public static int generate() {
        return RANDOM.nextInt(100 - 1) + 1;
    }

    @Override public String toString() {
        return "Percentage{" +
                "chance=" + chance +
                '}';
    }

    public static class Adapter extends SimpleAdapter<Percentage> {

        @Override
        public JsonElement serialize(Percentage percentage, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(percentage.chance + "%");
        }

        @Override
        public Percentage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber())
                return new Percentage(jsonElement.getAsInt());
            return new Percentage(Integer.parseInt(jsonElement.getAsString().replace("%", "")));
        }

    }
}