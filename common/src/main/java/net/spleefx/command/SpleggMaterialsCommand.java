package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.*;

@RegisteredCommand
@RegisterStrictlyFor("splegg")
public class SpleggMaterialsCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("materials")
                .description("Edit a list of destroyable/undestroyable splegg materials")
                .parameters("<list / add / remove> <arena> [<materials>]")
                .extensionCommand()
                .permission("spleefx.splegg.materials")
                .withHelpMenu(
                        "/{cmd} materials add <arena> <material names or IDs>",
                        "/{cmd} materials remove <arena> <material names or IDs>",
                        "/{cmd} materials list <arena>"
                )
                .checkIfArgsAre(atLeast(2))
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        String task = args.get(0);
        SpleggArena arena = args.arena(1);
        int oldSize = arena.getMaterials().size();
        switch (task) {
            case "add": {
                if (args.size() == 2)
                    return Response.error("Specify materials!");
                for (String mat : args.subList(2, args.size())) {
                    Material m = Material.matchMaterial(mat);
                    if (m == null)
                        sender.reply(arena, "&cInvalid material: &e" + mat + "&c.");
                    else if (arena.getMaterials().add(m))
                        sender.reply(extension, "&a+ &7" + m.name().toLowerCase());
                }
                if (oldSize == arena.getMaterials().size())
                    return Response.ok("&cNo materials have been modified.");
                return Response.ok();
            }
            case "remove":
            case "rm": {
                if (args.size() == 2)
                    return Response.error("Specify materials!");
                for (String mat : args.subList(2, args.size())) {
                    Material m = Material.matchMaterial(mat);
                    if (m == null)
                        sender.reply(arena, "&cInvalid material: &e" + mat + "&c.");
                    else if (arena.getMaterials().remove(m))
                        sender.reply(extension, "&c- &7" + m.name().toLowerCase());
                }
                if (oldSize == arena.getMaterials().size())
                    return Response.ok("&cNo materials have been modified.");
            }
            case "list":
            case "ls": {
                if (arena.getMaterials().isEmpty())
                    return Response.ok("&cNo materials.");
                for (Material m : arena.getMaterials())
                    sender.reply(arena, "&7- &e" + m.name().toLowerCase());
                return Response.ok();
            }
            default:
                return Response.invalidUsage();
        }
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion
                .start()
                .with(literal("add")
                        .then(arenas(extension)))
                .and(literal("list")
                        .then(arenas(extension)))
                .then(literal("remove")
                        .then(arenas(extension)
                                .then(listMaterials(args, 1)
                                        .then("materials to remove"))));
    }
}