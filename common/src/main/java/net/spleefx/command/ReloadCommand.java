package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.summary.template.GameSummaryTemplate;
import net.spleefx.config.json.MappedConfiguration;
import net.spleefx.core.command.*;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import net.spleefx.powerup.api.Powerups;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class ReloadCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("reload", "rl")
                .checkIfArgsAre(anything())
                .description("Reload the plugin's files")
                .permission("spleefx.admin.reload")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        Powerups.read(plugin);
        plugin.reloadConfig();
        plugin.getStatsFile().reload();
        plugin.getJoinGuiFile().reload();
        plugin.getConfigurationPack().refresh();
        plugin.getMessageManager().load(true);
        plugin.gameSummaryFile = new MappedConfiguration(plugin.getFileManager().createFile("game-summary.yml"));
        plugin.gameSummary = plugin.gameSummaryFile.getAs(GameSummaryTemplate.class);
        for (MatchExtension ex : Extensions.getExtensions()) {
            ex.reload();
        }
        return Response.ok("&aPlugin successfully reloaded!");
    }

}