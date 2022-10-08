package net.spleefx.arena.summary;

import com.google.gson.JsonObject;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.summary.template.RemoteSummaryTemplate;
import net.spleefx.backend.SpleefXWebAPI;
import net.spleefx.core.command.Mson;
import net.spleefx.event.arena.end.PreArenaEndEvent;
import net.spleefx.event.listen.EventListenerAdapter;
import net.spleefx.util.message.message.Message;

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
            SpleefXWebAPI.createStats(payload.toString()).whenComplete((url, e) -> {
                if (e != null) return;
                for (MatchPlayer player : event.getTrackedPlayers()) {
                    Mson message = Mson
                            .of(event.getPrefix() + Message.GAME_SUMMARY.getValue())
                            .url(url)
                            .tooltip(Message.CLICK_TO_GO_TO_SX_NET.getValue());
                    player.msg(message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
