/*
 * * Copyright 2020 github.com/ReflxctionDev
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
package net.spleefx.event.ability;

import lombok.Getter;
import net.spleefx.arena.MatchArena;
import net.spleefx.event.CancellableEvent;
import net.spleefx.event.player.PlayerArenaEvent;
import org.bukkit.entity.Player;

/**
 * Fired when a player double-jumps in an arena.
 */
@Getter
@CancellableEvent
public class PlayerDoubleJumpEvent extends PlayerArenaEvent {

    /**
     * The cancelled state of this arena
     */
    private boolean cancelled = false;

    /**
     * The amount double-jumps this player has left
     */
    private final int doubleJumpsLeft;

    public PlayerDoubleJumpEvent(Player player, int doubleJumpsLeft, MatchArena arena) {
        super(player, arena);
        this.doubleJumpsLeft = doubleJumpsLeft;
    }
}