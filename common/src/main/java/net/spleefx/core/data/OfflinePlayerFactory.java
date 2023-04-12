/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package net.spleefx.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.ToString;
import net.spleefx.backend.Schedulers;
import net.spleefx.config.SpleefXConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * A class for creating and retrieving {@link OfflinePlayer} instances which are resolved
 * asynchronously to provide full player info, unlike Bukkit's standard methods which either
 * mask the UUID or nullify the name.
 */
public class OfflinePlayerFactory {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * The Mojang endpoint
     */
    private static final String ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    /**
     * Gson to deserialize response data
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    /**
     * Retrieves the player from the Bukkit cache, or requests it asynchronously from the Mojang API.
     *
     * @param uuid UUID to resolve from
     * @return A possibly-unfinished future of the player.
     */
    public static CompletableFuture<String> getOrRequest(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null) return requestPlayer(uuid);
        return CompletableFuture.completedFuture(player.getName());
    }

    /**
     * Requests the player from the Mojang API
     *
     * @param uuid UUID of the player
     * @return A future of the player, after injecting it into Bukkit
     */
    public static CompletableFuture<String> requestPlayer(UUID uuid) {
        if (Bukkit.getOnlineMode() || !SpleefXConfig.PATCH_OFFLINE_BUG.get())
            return CompletableFuture.completedFuture(Bukkit.getOfflinePlayer(uuid).getName());
        CompletableFuture<String> future = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> {
            try {
                String url = String.format(ENDPOINT, uuid.toString().replace("-", ""));
                Request request = new Request.Builder()
                        .url(url)
                        .header("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseText = Objects.requireNonNull(response.body()).string();
                        ProfileResponse p = GSON.fromJson(responseText, ProfileResponse.class);
                        future.complete(p.name);
                    } else {
                        future.complete("NoName");
                    }
                }
            } catch (IOException e) {
                future.obtrudeException(e);
            }
        });
        return future;
    }

    // it's used by gson but whatever
    @SuppressWarnings("unused")
    @ToString
    private static class ProfileResponse {

        private UUID id;
        private String name;

    }

    private static class UUIDTypeAdapter extends TypeAdapter<UUID> {

        private static final Pattern STRIPPED_UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

        public void write(JsonWriter out, UUID value) throws IOException {
            out.value(fromUUID(value));
        }

        public UUID read(JsonReader in) throws IOException {
            return fromString(in.nextString());
        }

        public static String fromUUID(UUID uuid) {
            return uuid.toString().replace("-", "");
        }

        public static UUID fromString(String uuid) {
            return UUID.fromString(STRIPPED_UUID_PATTERN.matcher(uuid).replaceAll("$1-$2-$3-$4-$5"));
        }
    }


}
