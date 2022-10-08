package net.spleefx.arena.summary.template;

import com.google.gson.annotations.JsonAdapter;
import net.spleefx.extension.MatchExtension;
import net.spleefx.json.KeyedAdapters.ToStringKeyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSummaryTemplate {

    private final int sendReportIn = 30;

    @JsonAdapter(ToStringKeyMap.class)
    private final Map<MatchExtension, List<String>> summary = new HashMap<>();

    private final RemoteSummaryTemplate remoteSummary = new RemoteSummaryTemplate();

    public int reportDelay() {
        return sendReportIn;
    }

    public Map<MatchExtension, List<String>> getSummary() {
        return summary;
    }

    public RemoteSummaryTemplate getRemoteSummary() {
        return remoteSummary;
    }
}
