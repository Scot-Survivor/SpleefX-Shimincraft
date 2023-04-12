package net.spleefx.arena.summary;

import com.google.gson.JsonObject;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.summary.template.RemoteSummaryTemplate;
import net.spleefx.event.arena.end.PreArenaEndEvent;
import net.spleefx.event.listen.EventListenerAdapter;

import java.io.IOException;

@RegisteredListener(parameters = RegisteredListener.APP)
public class RemoteSummaryListener extends EventListenerAdapter {

    private final SpleefX plugin;

    public RemoteSummaryListener(SpleefX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPreArenaEnd(PreArenaEndEvent event) {
        if (event.isForcibly()) return;
        GameSummary summary = event.getGameSummary();
        RemoteSummaryTemplate remoteSummaryTemplate = plugin.gameSummary.getRemoteSummary();
        if (!remoteSummaryTemplate.isEnabled()) return;
        try {
            JsonObject payload = summary.asJson(event.getExtension());
            if (payload == null) return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
