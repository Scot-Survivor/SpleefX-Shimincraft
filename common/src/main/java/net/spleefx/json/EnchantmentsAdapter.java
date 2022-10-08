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
package net.spleefx.json;

import com.cryptomorin.xseries.XEnchantment;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.spleefx.SpleefX;
import net.spleefx.compatibility.PluginCompatibility;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnchantmentsAdapter extends SimpleAdapter<Map<Enchantment, Integer>> {

    public static final EnchantmentsAdapter INSTANCE = new EnchantmentsAdapter();

    public static final Type TYPE = new TypeToken<Map<Enchantment, Integer>>() {
    }.getType();

    @Override
    public Map<Enchantment, Integer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
        for (JsonElement element : array) {
            String[] data = element.getAsString().split(":");
            Enchantment e = XEnchantment.matchXEnchantment(data[0]).map(XEnchantment::getEnchant).orElse(null);
            if (e == null) {
                SpleefX.logger().warning("Unrecognizable enchantment: " + data[0]);
                continue;
            }
            enchantments.put(e, Integer.parseInt(data[1]));
        }
        return enchantments;
    }

    @Override
    public JsonElement serialize(Map<Enchantment, Integer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        src.forEach((ench, power) -> array.add(new JsonPrimitive(PluginCompatibility.attempt(() -> ench.getKey().getKey(), ench::getName) + ":" + power)));
        return array;
    }
}
