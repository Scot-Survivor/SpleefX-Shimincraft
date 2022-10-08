package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.extension.MatchExtension;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.*;

@RegisteredCommand
public class TeamsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("teams", "team")
                .permission("spleefx.admin.{ext}.teams")
                .checkIfArgsAre(atLeast(2))
                .extensionCommand()
                .description("Manage teams for an arena")
                .parameters("<add / remove / list>")
                .withHelpMenu(
                        "/{cmd} teams add &e<arena key> &a<team key> - Add a team to an arena",
                        "/{cmd} teams remove &e<arena key> &a<team key> - Remove a team from an arena",
                        "/{cmd} teams list &e<arena key> - List all teams in an arena"
                )
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MatchArena arena = args.arena(1);
        if (!arena.isTeams()) return Response.error("&cArena &e%s &cis an FFA arena!", arena.getKey());
        switch (args.get(0).toLowerCase()) {
            case "add": {
                MatchTeam team = args.team(2);
                if (arena.getTeams().add(team))
                    return Response.ok("&aSuccessfully added team &e%s &ato arena &e%s&a.", team.getDisplayName(), arena.getKey());
                return Response.ok("&cArena &e%s &calready contains this team!", arena.getKey());
            }
            case "remove": {
                MatchTeam team = args.team(2);
                if (arena.getTeams().remove(team))
                    return Response.ok("&aSuccessfully removed team &e%s &afrom arena &e%s&a.", team.getDisplayName(), arena.getKey());
                return Response.ok("&cArena &e%s &cdoes not contains this team!", arena.getKey());
            }
            case "list": {
                for (MatchTeam team : arena.getTeams()) {
                    sender.reply(arena, "&7- &b" + team.getDisplayName() + " &a(%s)", team.getKey());
                }
                return Response.ok();
            }
            default:
                return Response.invalidUsage();
        }
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return start()
                .then(literal("add")
                        .then(arenas(extension)
                                .then(notTeams(args, 1))))
                .then(literal("remove")
                        .then(arenas(extension)
                                .then(teams(args, 1))))
                .then(literal("list")
                        .then(arenas(extension)));
    }
}
