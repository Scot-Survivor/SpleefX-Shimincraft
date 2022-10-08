/*
 * This file is part of SpleefX, licensed under the MIT License.
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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import net.spleefx.json.SpleefXGson;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class MappedConfiguration {

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private JsonObject data;
    private final Path file;
    private final Gson gson;
    private final Yaml yaml;

    public MappedConfiguration(File file) {
        this(file, SpleefXGson.MAIN);
    }

    public MappedConfiguration(File file, Gson gson) {
        this.file = file.toPath();
        this.gson = gson;
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        yaml = new Yaml(options);

        load();
    }

    @SneakyThrows public void load() {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Map<String, Object> map = yaml.load(reader);
            if (map == null) map = new HashMap<>();
            data = gson.toJsonTree(map, MAP_TYPE).getAsJsonObject();
        }
    }

    public <T> T get(String key, Type type) {
        return gson.fromJson(data.get(key), type);
    }

    public <T> T getAs(Type type) {
        return gson.fromJson(data, type);
    }

    public <T> T get(String key, Class<T> type) {
        return get(key, (Type) type);
    }

    public void set(String key, Object v) {
        data.add(key, gson.toJsonTree(v));
    }

    public void set(String key, Object v, Type type) {
        data.add(key, gson.toJsonTree(v, type));
    }

    public boolean contains(String path) {
        return data.keySet().contains(path);
    }

    public void setData(JsonObject jsonObject) {
        this.data = jsonObject;
    }

    public JsonObject getData() {
        return data;
    }

    @SneakyThrows public void save() {
        Map<String, Object> dataToMap = gson.fromJson(data, MAP_TYPE);
        StringWriter output = new StringWriter();
        yaml.dump(dataToMap, output);
        String content = output.toString();
        Files.write(file, content.getBytes(UTF_8), WRITE, TRUNCATE_EXISTING);

//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
//            yaml.dump(dataToMap, writer);
//        }
    }

}
