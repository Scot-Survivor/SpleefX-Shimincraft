package net.spleefx.command;

import com.sk89q.worldedit.EmptyClipboardException;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.MatchArena;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import net.spleefx.hook.worldedit.SchematicManager;
import net.spleefx.model.Position;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.spleefx.core.command.tab.TabCompletion.arenas;

@RegisteredCommand
public class EditBuildingCommand extends BaseCommand {

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("editbuilding", "editarena")
                .extensionCommand()
                .description("Edit the schematic (building) of an arena")
                .parameters("<arena key>")
                .permission("spleefx.{ext}.editbuilding")
                .checkIfArgsAre(equalTo(1))
                .requirePlayer()
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        MatchArena arena = args.arena(0);
        Player player = sender.player();
        try {
            SchematicManager processor = SpleefX.newSchematicManager(arena.getKey());
            processor.write(SpleefX.getSpleefX().getWorldEdit().getSession(player).getClipboard());
            Position origin = SchematicManager.getOrigin(sender.player().getWorld(), plugin.getWorldEdit().getSession(sender.player()).getClipboard());
            arena.setOrigin(origin);
            return Response.ok("&aSuccessfully overrided schematic building for arena &e" + arena.getKey() + "&a.");
        } catch (EmptyClipboardException e) {
            return Response.error("&cYou must select and copy the arena to your clipboard (with WorldEdit)!");
        }
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(arenas(extension));
    }
}
