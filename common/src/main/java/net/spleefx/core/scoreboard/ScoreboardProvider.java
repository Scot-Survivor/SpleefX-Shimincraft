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
package net.spleefx.core.scoreboard;

import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.extension.MatchScoreboard;
import net.spleefx.util.game.Chat;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardProvider {

    public String getTitle(Player player) {
        try {
            return Chat.colorize(getMatchScoreboard(MatchPlayer.wrap(player)).getTitle());
        } catch (NullPointerException e) {
            return null;
        }
    }

    public List<String> getLines(Player player) {
        MatchPlayer p = MatchPlayer.wrap(player);
        ReloadedArenaEngine engine = p.getArena().getEngine();
        MatchScoreboard holder = getMatchScoreboard(p);
        if (holder == null)
            throw new RuntimeException("Cannot find a scoreboard section for extension " + p.getArena().getExtension().getKey() + ". Please add a section to remove this error.");
        return holder.getText()
                .stream()
                .map(s -> s.trim().isEmpty() ? "" : MatchScoreboard.replacePlaceholders(p, s, p.getArena()))
                .collect(Collectors.toList());
    }

    private static MatchScoreboard getMatchScoreboard(MatchPlayer p) {
        return p.getArena().getExtension().getScoreboard().get(p.getArena().getEngine().getCurrentScoreboard());
    }

}