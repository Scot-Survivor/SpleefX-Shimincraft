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
package net.spleefx.legacy;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.spleefx.hook.worldedit.NoSchematicException;
import net.spleefx.hook.worldedit.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static net.spleefx.backend.Schedulers.sneakyThrow;

public class FAWESchematicManager extends WESchematicManager {

    public FAWESchematicManager() {
    }

    private FAWESchematicManager(WorldEditPlugin plugin, String name, File directory) {
        super(plugin, name, directory);
    }

    @Override
    public void write(ClipboardHolder clipboard) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> super.write(clipboard));
    }

    @Override
    public CompletableFuture<Void> paste(Location location) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                super.paste(location);
                future.complete(null);
            } catch (NoSchematicException e) {
                throw sneakyThrow(e);
            }
        });
        return future;
    }

    /**
     * Creates a new instance of the processor
     *
     * @param plugin    Plugin instance
     * @param name      Name of the schematic
     * @param directory The directory of the schematic processor
     * @return The newly created schematic processor
     */
    @Override
    public SchematicManager newInstance(WorldEditPlugin plugin, String name, File directory) {
        return new FAWESchematicManager(plugin, name, directory);
    }
}
