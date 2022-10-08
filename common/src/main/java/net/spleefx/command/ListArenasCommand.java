package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.*;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.message.message.Message;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegisteredCommand
public class ListArenasCommand extends BaseCommand {

    private static final ChatComponent DASH = new ChatComponent().setText("&7-");

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("listarenas", "list")
                .description("List all arenas of a specific game-mode.")
                .permission("spleefx.{ext}.listarenas", PermissionDefault.TRUE)
                .checkIfArgsAre(zero())
                .extensionCommand()
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        List<MatchArena> arenas = Arenas.getByExtension(extension);
        String command = args.getCommand().getName();
        if (arenas.isEmpty()) {
            return Response.error(Message.NO_ARENAS, extension);
        }
        if (sender.isOp()) {
            if (sender.isPlayer()) {
                arenas.forEach(arena -> {
                    String key = arena.getKey();
                    ComponentJSON json = new ComponentJSON()
                            .append(Mson.of("&e%s &7- %s &7- ", arena.getKey(), arena.getEngine().getStage().getState()))
                            .append(Mson.border("&6Join").tooltip("&eClick to join the arena").execute("/%s join %s", command, key))
                            .space().append(DASH).space()
                            .append(Mson.border("&bSettings").tooltip("&eClick to open the settings GUI").execute("/%s arena settings %s", command, key))
                            .space().append(DASH).space()
                            .append(Mson.border("&aRegenerate").tooltip("&eClick to regenerate the arena").execute("/%s arena regenerate %s", command, key))
                            .space().append(DASH).space()
                            .append(Mson.border("&cRemove").tooltip("&eClick to remove this arena").execute("/%s arena remove %s", command, key));
                    sender.reply(json);
                });
                return Response.ok();
            } else { // sender is console
                arenas.forEach(arena -> sender.reply("&e%s &7- %s", arena.getKey(), arena.getEngine().getStage().getState()));
            }
        } else { // not op
            arenas.forEach(arena -> {
                ComponentJSON json = new ComponentJSON().append(Mson.of("&e%s &7- %s", arena.getDisplayName(), arena.getEngine().getStage().getState()));
                json.space().append(Mson.border("&6Join").tooltip("&eClick to join the arena").execute("/%s join %s", command, arena.getKey()));
                sender.reply(json);
            });
        }
        return Response.ok();
    }
}