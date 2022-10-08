package net.spleefx.arena.summary.template;

import com.google.gson.annotations.JsonAdapter;
import lombok.Getter;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.json.KeyedAdapters;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Getter
public class RemoteSummaryTemplate {

    private boolean enabled = true;
    private String serverName = "";
    private String serverIP = "";
    private String victoryText = "VICTORY", eliminatedText = "ELIMINATED", carriedText = "CARRIED";

    @JsonAdapter(KeyedAdapters.ToStringKeyMap.class)
    private final Map<MatchExtension, RemoteGameTemplate> modes = Collections.emptyMap();

    @GsonHook
    @Getter
    public static class RemoteGameTemplate {

        private final List<String> backgroundURLs = emptyList();
        private final List<String> header = emptyList();
        private final List<String> playerFormat = emptyList();

        {
            checkSameSize();
        }

        @AfterDeserialization
        private void checkSameSize() {
            if (header.size() != playerFormat.size())
                throw new IllegalStateException("Header size and PlayerFormat size are not the same! Header: " + header.size() + ", PlayerFormat: " + playerFormat.size());
        }
    }

}
