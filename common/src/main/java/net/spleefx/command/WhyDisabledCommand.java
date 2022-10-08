package net.spleefx.command;

import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.MatchArena;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import net.spleefx.model.Position;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static net.spleefx.core.command.tab.TabCompletion.arenas;

@RegisteredCommand
public class WhyDisabledCommand extends BaseCommand {

    private static final ComponentJSON JSON = new ComponentJSON();

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("whydisabled")
                .extensionCommand()
                .description("List all violations of an arena (get why it's disabled.)")
                .checkIfArgsAre(equalTo(1))
                .permission("spleefx.{ext}.whydisabled")
                .parameters("<arena>")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        requireNonNull(extension, "extension is null!");

        MatchArena arena = args.arena(0);
        Set<String> violations = getViolations(arena);
        if (violations.isEmpty()) {
            return Response.ok("&aThis arena is ready, as everything has been set!");
        }
        sender.reply(arena, "&cViolations:");
        if (violations.remove("Arena is disabled [Enable]")) {
            JSON.clear()
                    .append(Mson.of(extension.getPrefix() + " &7- &cArena is disabled "))
                    .append(Mson.of("&a&l[Enable]").tooltip("Click to enable").execute("/%s arena settings %s enable", args.getCommand().getName(), arena.getKey()));
            sender.reply(JSON);
            JSON.clear();
        }
        violations.forEach(v -> sender.reply(extension, "&7- &c" + v));
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.of(arenas(extension));
    }

    private static Set<String> getViolations(MatchArena arena) {
        Set<String> violations = new HashSet<>();
        if (!arena.isEnabled()) {
            violations.add("Arena is disabled [Enable]");
        }
        if (arena.getLobby() == null /*&& !SpleefXConfig.MATCHMAKING.get()*/) {
            violations.add("§eNo lobby has been set");
        }
        if (arena.getMinimum() > arena.getMaximum()) {
            violations.add("Arena minimum player count is greater than the maximum count");
        }
        if (arena.getMinimum() < 2) {
            violations.add("Minimum is less than 2 players");
        }
        if (arena.isFFA()) {
            if ((arena.getMaxPlayerCount() < 2))
                violations.add("Max player count is less than 2");
        }/* else {
            if (arena.getLobby() == null && SpleefXConfig.MATCHMAKING.get()) {
                violations.add("No lobby has been set (matchmaking in enabled)");
            }
        }*/
        Map<?, Position> spawnPoints = arena.isTeams() ? arena.getSpawnPoints() : arena.getFfaSettings().getSpawnPoints();
        if (!(spawnPoints.size() >= arena.getMaximum())) {
            int delta = arena.getMaximum() - spawnPoints.size();
            violations.add("Not all spawn-points have been set §e(§a" + spawnPoints.size() + " §eset, §7" + delta + " §eleft)");
        }
        return violations;
    }
}