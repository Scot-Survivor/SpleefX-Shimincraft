/*
 * This file is part of SpleefX, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.spleefx.command;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Enums;
import net.spleefx.SXBukkitBootstrap;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.config.SpleefXConfig;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.data.*;
import net.spleefx.core.data.PlayerCacheManager.IConnectable;
import net.spleefx.core.data.impl.ForwardingCacheManager;
import net.spleefx.extension.MatchExtension;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.spleefx.core.command.tab.TabCompletion.list;
import static net.spleefx.core.command.tab.TabCompletion.of;

@RegisteredCommand
public class ConvertDataCommand extends BaseCommand {

    private static final List<String> SUGGESTIONS = Arrays.stream(StorageType.values())
            .filter(c -> c != SpleefXConfig.STORAGE_METHOD.get())
            .map(StorageType::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("convertdata")
                .description("Convert player data from one type to another")
                .checkIfArgsAre(lessThan(3))
                .permission("spleefx.command.convertdata")
                .parameters("<new data type>")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        StorageType type = Enums.getIfPresent(StorageType.class, args.get(0).toUpperCase()).orNull();
        if (type == null) {
            return Response.error("Invalid storage type: &e" + type);
        }
        if (type == SpleefXConfig.STORAGE_METHOD.get()) {
            return Response.error("You're already on &e" + type + "&c!");
        }
        if (!args.flag("confirm")) {
            return Response.error("This command will cause the plugin to load the data of ALL players. Are you sure? If so, run &e/spleefx convertdata " + type.name().toLowerCase() + " -confirm&c.");
        }
        sender.reply("&eDownloading any required drivers...");
        ((SXBukkitBootstrap) SpleefX.getPlugin()).getDependencyManager().loadStorageDependencies(type);
        sender.reply("&eConverting...");
        PlayerCacheManager cacheManager = StorageMapping.valueOf(type.name()).getCacheManager();
        cacheManager.init(plugin);
        if (cacheManager instanceof IConnectable)
            ((IConnectable) cacheManager).connect();
        cacheManager.executor().execute(() -> {
            try {
                LoadingCache<UUID, PlayerProfile> cache = PlayerRepository.REPOSITORY.getCache();
                ForwardingCacheManager.delegate().cacheAll(cache);
                cacheManager.writeAll(cache.asMap(), true);
                cacheManager.shutdown(plugin);
                sender.reply("&aData successfully converted!");
            } catch (Throwable t) {
                sender.reply("&cFailed to convert data. Check console for errors");
                t.printStackTrace();
            }
        });
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return of(list(SUGGESTIONS).then("-confirm"));
    }
}
