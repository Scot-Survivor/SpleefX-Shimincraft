package net.spleefx.backend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.spleefx.SpleefX;
import net.spleefx.json.SpleefXGson;
import okhttp3.*;
import okhttp3.Request.Builder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.spleefx.util.Util.n;

public class SpleefXWebAPI {

    private static final JsonParser PARSER = new JsonParser();

    /**
     * The HTTP client to send and listen to requests
     */
    public static final OkHttpClient CLIENT = new OkHttpClient();

    private static final String API_ENDPOINT = "https://spleefx.net/api/";
    private static final String DEBUG_ENDPOINT = "https://spleefx.net/api/create-debug";
    private static final String DEBUG = "https://spleefx.net/api/debug/";
    private static final String STATS_ENDPOINT = "https://spleefx.net/stats";

    public static List<String> getVersionList() {
        Request request = new Builder()
                .url(API_ENDPOINT + "version")
                .header("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return Collections.singletonList(SpleefX.getPlugin().getDescription().getVersion());
            }
            String responseBody = n(response.body(), "response was successful but #body() is null!").string();
            return SpleefXGson.MAIN.fromJson(responseBody, new TypeToken<List<String>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.singletonList(SpleefX.getPlugin().getDescription().getVersion());
        }
    }

    public static CompletableFuture<String> createStats(String payload) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), payload);
            Request request = new Builder().url("https://spleefx.net/api/create-stats").post(body).build();
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    new IllegalStateException("Unsuccessful").printStackTrace();
                    future.complete("Error occured: Unsuccessful");
                }
                future.complete(STATS_ENDPOINT + "/" + n(response.body()).string());
            } catch (IOException e) {
                e.printStackTrace();
                future.complete("Error occured: " + e.getMessage());
            }
        });
        return future;
    }

    public static CompletableFuture<String> createDebug(String content) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Schedulers.POOL.submit(() -> {
            JsonObject o = new JsonObject();
            o.addProperty("json", content);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), o.toString());
            Request request = new Builder().url(DEBUG_ENDPOINT).post(body).build();
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    new IllegalStateException("Unsuccessful").printStackTrace();
                    future.complete("Error occured: Unsuccessful");
                }

                future.complete(DEBUG + PARSER.parse(n(response.body()).string()).getAsJsonObject().get("id").getAsString());
            } catch (IOException e) {
                e.printStackTrace();
                future.complete("Error occured: " + e.getMessage());
            }
        });
        return future;
    }
}