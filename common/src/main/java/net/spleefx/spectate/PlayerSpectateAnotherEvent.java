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
package net.spleefx.spectate;

import net.spleefx.arena.MatchArena;
import net.spleefx.event.SpleefXEvent;
import org.bukkit.entity.Player;

public class PlayerSpectateAnotherEvent extends SpleefXEvent {

    private final Player spectator;
    private final Player target;
    private final MatchArena arena;

    /**
     * Invoked when a player spectates another player in an arena
     *
     * @param spectator The spectator
     * @param target    The player being spectated
     * @param arena     The arena
     */
    public PlayerSpectateAnotherEvent(Player spectator, Player target, MatchArena arena) {
        this.spectator = spectator;
        this.target = target;
        this.arena = arena;
    }

    public Player getSpectator() {
        return spectator;
    }

    public Player getTarget() {
        return target;
    }

    public MatchArena getArena() {
        return arena;
    }

}
