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
package net.spleefx.event.arena;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.core.command.Prefixable;
import net.spleefx.event.SpleefXEvent;
import net.spleefx.extension.MatchExtension;
import org.jetbrains.annotations.NotNull;

public abstract class ArenaEvent extends SpleefXEvent implements Prefixable {

    @NotNull
    protected final MatchArena arena;

    @NotNull
    protected final MatchExtension extension;

    public ArenaEvent(MatchArena arena) {
        this.arena = arena;
        extension = arena.getExtension();
    }

    public @NotNull ReloadedArenaEngine getEngine() {
        return arena.getEngine();
    }

    public @NotNull MatchArena getArena() {
        return arena;
    }

    public @NotNull MatchExtension getExtension() {
        return extension;
    }

    public @NotNull String getPrefix() {
        return extension.getPrefix();
    }
}
