/*
 * This file is part of jsonbutyml, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.spleefx.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.spleefx.json.SpleefXGson;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ObjectConfiguration {

    protected static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    protected final Gson gson;
    protected final File file;
    protected volatile Map<String, Object> data = new HashMap<>();
    protected final Yaml yaml;

    public ObjectConfiguration(File file) {
        this(SpleefXGson.MAIN, file);
    }

    public ObjectConfiguration(Gson gson, File file) {
        this.gson = gson;
        this.file = file;
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
        load();
    }

    public void load() {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            data = yaml.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T getAs(Type type) {
        JsonElement json = gson.toJsonTree(data, MAP_TYPE);
        return gson.fromJson(json, type);
    }

    public <T> T getAs(Class<T> type) { // for type inference
        return getAs((Type) type);
    }

    public <T> T get(String path, Type type) {
        JsonElement json = gson.toJsonTree(data.get(path));
        return gson.fromJson(json, type);
    }

    public void set(String path, Object value) {
        if (value == null) {
            data.remove(path);
            return;
        }
        data.put(path, value);
    }

    public boolean contains(String path) {
        return data.containsKey(path);
    }

    public void copy(JsonObject json) {
        data = gson.fromJson(json, MAP_TYPE);
    }

    public void save() {
        JsonObject json = toJsonObject();
        Map<String, Object> reversed = gson.fromJson(json, MAP_TYPE);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.WRITE)) {
            yaml.dump(reversed, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject toJsonObject() {
        return gson.toJsonTree(data, MAP_TYPE).getAsJsonObject();
    }

}
