package net.spleefx.command;

import com.cryptomorin.xseries.XMaterial;
import com.sk89q.worldedit.EmptyClipboardException;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.arena.ArenaType;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.SpawnPointScanner;
import net.spleefx.arena.engine.SpawnPointHelper;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.MatchExtension;
import net.spleefx.gui.ArenaSettingsUI;
import net.spleefx.hook.worldedit.SchematicManager;
import net.spleefx.model.Position;
import net.spleefx.util.Placeholders.CommandEntry;
import net.spleefx.util.message.message.Message;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static net.spleefx.core.command.tab.TabCompletion.*;
import static net.spleefx.util.FileManager.isValidPath;

@RegisteredCommand
public class ArenaCommand extends BaseCommand {

    @Override
    protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("arena", "arenas")
                .permission("spleefx.admin.{ext}.arena")
                .description("Commands to manage arenas")
                .parameters("help")
                .withHelpMenu(
                        "/{cmd} arena help - List all commands",
                        "/{cmd} arena create &e<arena key> <arena display name> - Create an arena with the tracked key and a display name.",
                        "/{cmd} arena remove &e<arena key> - Delete the arena with the specified key",
                        "/{cmd} arena sign &e<arena key> - Add a sign to the arena",
                        "/{cmd} arena settings &e<arena key> [optional: setting key] [setting value] - Display the settings GUI (or edit a setting by key)",
                        "/{cmd} arena lobby &e<arena key> [optional: team or player no.] - Set the lobby (globally or for a specific team/player)",
                        "/{cmd} arena powerupscenter &e<arena key> - Set the center to distribute power-ups around",
                        "/{cmd} arena removelobby &e<arena key> [optional: team or player no.] - Remove the lobby (globally or for a specific team/player)",
                        "/{cmd} arena spawnpoint &e<arena key> <team or player no.> - Set the spawnpoint of a specified team/player",
                        "/{cmd} arena finishingloc &e<arena key> - Set the finishing location of an arena",
                        "/{cmd} arena regenerate &e<arena key> - Regenerate an arena"
                )
                .extensionCommand()
                .checkIfArgsAre(atLeast(1))
                .build();
    }

    //@formatter:off
    @SuppressWarnings("RedundantCast")@Override
    public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension extension) throws CommandException {
        if (args.size() == 1) {
            if ("help".equals(args.get(0))) return Response.sendHelp();
        }
        else if (args.size() == 2) {
            switch (args.get(0)) {
                case "delete":
                case "remove":
                case "rm":
                case "yeet": {
                    String key = args.get(1);
                    MatchArena deleted = Arenas.deleteArena(key);
                    if (deleted == null) {
                        return Response.error(Message.INVALID_ARENA, extension, new CommandEntry(null, key));
                    }
                    return Response.ok(Message.ARENA_DELETED, extension, deleted);
                }
                case "powerupscenter": {
                    MatchArena arena = args.arena(1);
                    Position center = args.centerize(sender.location());
                    arena.setPowerupsCenter(center);
                    return Response.ok("&aSuccessfully set the power-ups center to your location");
                }
                case "lobby":
                case "setlobby": {
                    MatchArena arena = args.arena(1);
                    Position lobby = args.centerize(sender.location());
                    arena.setLobby(lobby);
                    return Response.ok(Message.LOBBY_SET, arena, lobby, extension);
                }
                case "finishingloc":
                case "fallbackloc":
                case "endingloc":
                case "setfinishingloc": {
                    MatchArena arena = args.arena(1);
                    Position loc = args.centerize(sender.location());
                    arena.setFinishingLocation(loc);
                    return Response.ok("&aArena &e%s&a's finishing location has been set " +
                                    "to &e%.1f&a, &e%.1f&a, &e%.1f&a.",
                            arena.getKey(), loc.x, loc.y, loc.z);
                }
                case "removefinishingloc":
                case "rmfinishinglocation": {
                    MatchArena arena = args.arena(1);
                    arena.setFinishingLocation(null);
                    return Response.ok("&aFinishing location for arena &e" + arena.getKey() + " &ahas been removed.");
                }
                case "regenerate":
                case "regen":
                case "restore": {
                    MatchArena arena = args.arena(1);
                    sender.reply(arena, "&eRegenerating...");
                    arena.getEngine().regenerate().thenRun(() -> sender.reply(extension,"&aArena &e" + arena.getKey() + " &ahas been regenerated."));
                    return Response.ok();
                }
                case "spectatingpoint": {
                    Position loc = args.centerize(sender.location());
                    MatchArena arena = args.arena(1);
                    arena.setSpectatingPoint(loc);
                    return Response.ok("&aArena &e%s&a's spectating point has been set " +
                                    "to &e%.1f&a, &e%.1f&a, &e%.1f&a.",
                            arena.getKey(), loc.x, loc.y, loc.z);
                }
                case "removespectatingpoint": {
                    MatchArena arena = args.arena(1);
                    arena.setSpectatingPoint(null);
                    return Response.ok("&aSpectating point for arena &e" + arena.getKey() + " &ahas been removed.");
                }
                case "settings":
                case "option":
                case "options": {
                    MatchArena arena = args.arena(1);
                    return Response.ignore(new ArenaSettingsUI(arena, sender.player()));
                }
                case "sign": {
                    MatchArena arena = args.arena(1);
                    Block block = sender.player().getTargetBlock((Set<Material>) null, 100);
                    if (!block.getType().name().contains("SIGN"))
                        return Response.error("&cYou must look at a sign!");
                    Position at = Position.at(block);
                    arena.getSigns().add(at);
                    arena.getSignHandler().updateSign(at);
                    return Response.ok("&aSuccessfully added sign to arena &e%s&a.", arena.getKey());
                }
                case "joingui":
                case "gui":
                case "joinguiitem": {
                    MatchArena arena = args.arena(1);
                    ItemStack mainHand = sender.getMainHand();
                    if (mainHand == null || mainHand.getType() == Material.AIR) {
                        return Response.error("&cYou must hold the item in your main hand!");
                    }
                    arena.setJoinGUIItem(XMaterial.matchXMaterial(mainHand));
                    return Response.ok("&aSuccessfully updated the display item for arena &e%s&a.", arena.getKey());
                }
            }
        }
        else if (args.size() == 3) {
            switch (args.get(0)) {
                case "settings":
                case "options":
                case "option": {
                    MatchArena arena = args.arena(1);
                    String option = args.get(2).toLowerCase();
                    switch (option) {
                        case "toggle":
                            arena.setEnabled(!arena.isEnabled());
                            arena.getEngine().getStage();
                            return Response.ok(arena.isEnabled() ? "&aArena &e%s &ahas been enabled" : "&cArena &e%s &chas been disabled", arena.getKey());
                        case "enable":
                            arena.setEnabled(true);
                            arena.getEngine().getStage();
                            return Response.ok("&aArena &e" + arena.getKey() + " &ahas been enabled");
                        case "disable":
                            arena.setEnabled(false);
                            arena.getEngine().getStage();
                            return Response.ok("&cArena &e" + arena.getKey() + " &chas been disabled");
                        default:
                            return Response.error("Invalid setting: &e" + option + "&c.");
                    }
                }
                case "spawnpoint":
                case "sp": {
                    MatchArena arena = args.arena(1);
                    if (arena.isFFA()) {
                        SpawnPointHelper ffaManager = arena.getFfaSettings();
                        if (args.get(2).equalsIgnoreCase("scan")) {
                            SpawnPointScanner.newState(sender.player(), arena, args);
                            return Response.ok();
                        }
                        if (args.get(2).equalsIgnoreCase("all")) {
                            for (int index = 1; index <= arena.getMaximum(); index++) {
                                Position sp = args.centerize(sender.location());
                                ffaManager.registerSpawnpoint(index, sp);
                            }
                            return Response.ok("&aSuccessfully set all spawn-points &e(1.." + arena.getMaximum() + ") &ato your location.");
                        }
                        int index = args.integer(2);
                        if (index > arena.getMaximum())
                            return Response.error("&cValue &e%s &cis greater than the arena's maximum (&b%s&c).", index, arena.getMaximum());
                        Position sp = args.centerize(sender.location());
                        ffaManager.registerSpawnpoint(index, sp);
                        Mson.of(extension.getChatPrefix() + "&eProtip: &aYou can use beacons instead of manually inputting every index individually. Click here for more info.")
                                .url("https://github.com/SpleefX/SpleefX/wiki/Spawn-Point-Scanner");
                        return Response.ok("&aSpawnpoint for index &e" + index + " &ahas been set to &e%s&a, &e%s&a, &e%s&a.", sp.x, sp.y, sp.z);
                    } else {
                        if (args.get(2).equalsIgnoreCase("all")) {
                            for (MatchTeam team : arena.getTeams()) {
                                Position sp = args.centerize(sender.location());
                                arena.getSpawnPoints().put(team, sp);
                            }
                            return Response.ok("&aSuccessfully set the spawn-points of all teams to your location.");
                        }
                        MatchTeam team = args.team(2);
                        if (!arena.getTeams().contains(team))
                            return Response.error(Message.TEAM_NOT_REGISTERED, team, extension);
                        Position sp = args.centerize(sender.location());
                        arena.getSpawnPoints().put(team, sp);
                        return Response.ok(Message.SPAWNPOINT_SET, arena, team, sp);
                    }
                }
                case "lobby":
                case "setlobby":
                case "teamlobby":
                case "playerlobby": {
                    MatchArena arena = args.arena(1);
                    if (arena.isFFA()) {
                        SpawnPointHelper ffaManager = arena.getFfaSettings();
                        int index = args.integer(2);
                        if (index > arena.getMaximum())
                            return Response.error("&cValue &e%s &cis greater than the arena's maximum (&b%s&c).", index, arena.getMaximum());
                        Position spawn = args.centerize(sender.location());
                        ffaManager.registerSpawnpoint(index, spawn);
                        return Response.ok("&aLobby for index &e" + index + " &ahas been set to &e%s&a, &e%s&a, &e%s&a.", spawn.x, spawn.y, spawn.z);
                    } else {
//                        if (SpleefXConfig.MATCHMAKING.get())
//                            return Response.error("&cYou cannot set team lobbies when matchmaking is enabled!");
                        MatchTeam team = args.team(2);
                        if (!arena.getTeams().contains(team))
                            return Response.error(Message.TEAM_NOT_REGISTERED, team, extension);
                        Position sp = args.centerize(sender.location());
                        arena.getTeamLobbies().put(team, sp);
                        return Response.ok("&aLobby for team &e" + team.getDisplayName() + " &ahas been set to &e%.1f&a, &e%.1f&a, &e%.1f&a.", sp.x, sp.y, sp.z);
                    }
                }
                case "finishingloc": {
                    return Response.invalidUsage();
                }
                case "removelobby":
                case "rmlobby":
                case "deletelobby": {
                    MatchArena arena = args.arena(1);
                    if (arena.isFFA()) {
                        SpawnPointHelper ffaManager = arena.getFfaSettings();
                        int index = args.integer(2);
                        if (index > arena.getMaximum())
                            return Response.error("&cValue &e%s &cis greater than the arena's maximum (&b%s&c).", index, arena.getMaximum());
                        ffaManager.removeLobby(index);
                        return Response.ok("&aLobby for index &e" + index + " &ahas been removed.");
                    } else {
                        MatchTeam team = args.team(2);
                        if (!arena.getTeams().contains(team))
                            return Response.error(Message.TEAM_NOT_REGISTERED, team, extension);
                        arena.getTeamLobbies().remove(team);
                        return Response.ok("&aLobby for team &e" + team.getDisplayName() + " &ahas been removed.");
                    }
                }
            }
        }
        else {
            switch (args.get(0)) {
                case "create":
                case "make":
                case "add": {
                    String key = args.get(1);
                    MatchArena arena = MatchArena.getByKey(key);
                    String displayName = args.combine(3);
                    if (arena != null) { // An arena with that key already exists
                        return Response.error(Message.ARENA_ALREADY_EXISTS, arena);
                    }
                    if (!isValidPath(key)) {
                        return Response.error("&cInvalid arena key: &e" + key + "&c.");
                    }
                    try {
                        Position origin = SchematicManager.getOrigin(sender.player().getWorld(), plugin.getWorldEdit().getSession(sender.player()).getClipboard());
                        arena = Arenas.createArena(key, displayName, origin, ArenaType.lookup(args.get(2)), extension);
                        if (arena.isFFA()) {
                            arena.setMaxPlayerCount(2);
                        }
                        SchematicManager sm = SpleefX.newSchematicManager(arena.getKey());
                        sm.write(plugin.getWorldEdit().getSession(sender.player()).getClipboard());
                        new ArenaSettingsUI(arena, sender.player());
                        return Response.ok(Message.ARENA_CREATED, arena, arena.getExtension(), new CommandEntry(args.command.getName()));
                    } catch (EmptyClipboardException e) {
                        return Response.error("&cYou must select and copy the arena to your clipboard (with WorldEdit)!");
                    }
                }
                case "settings":
                case "options":
                case "option": {
                    MatchArena arena = args.arena(1);
                    String option = args.get(2);
                    switch (option.toLowerCase()) {
                        case "name":
                        case "displayname": {
                            String displayName = args.combine(3);
                            arena.setDisplayName(displayName);
                            return Response.ok("&aDisplay name of arena &e%s &ahas been set to &d%s&a.", arena.getKey(), displayName);
                        }
                        case "membersperteam":
                        case "perteam":
                        case "members": {
                            int value = args.integerMin(3, 1);
                            arena.setMembersPerTeam(value);
                            return Response.ok("&aArena &e%s&a's members per team count has been set to &e%d", arena.getKey(), value);
                        }
                        case "time":
                        case "gametime":
                        case "timer": {
                            int value = args.integerMin(3, 1);
                            arena.setGameTime(value);
                            return Response.ok("&aArena &e%s&a's game time has been set to &e%d", arena.getKey(), value);
                        }
                        case "deathlevel":
                        case "deathzone": {
                            int value = args.integer(3);
                            arena.setDeathLevel(value);
                            return Response.ok("&aArena &e%s&a's death level has been set to &e%d", arena.getKey(), value);
                        }
                        case "powerupsradius": {
                            int value = args.integerMin(3, 1);
                            arena.setPowerupsRadius(value);
                            return Response.ok("&aArena &e%s&a's power-up distribution radius has been set to &e%d", arena.getKey(), value);
                        }
                        case "minimum":
                        case "min": {
                            int value = args.integerMin(3, 2);
                            arena.setMinimum(value);
                            return Response.ok("&aArena &e%s&a's minimum player count has been set to &e%d", arena.getKey(), value);
                        }
                        case "maxplayercount":
                        case "max": {
                            int value = args.integerMin(3, 2);
                            arena.setMaxPlayerCount(value);
                            return Response.ok("&aArena &e%s&a's max player count has been set to &e%d", arena.getKey(), value);
                        }
                    }
                }
            }
        }
        return Response.invalidUsage();
    }
    //@formatter:on

    @Override
    public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        return TabCompletion.start()
                .with(literal("help"))
                .then(literal("create")
                        .then(empty()
                                .then(list("ffa", "teams"))))
                .and(literal("remove")
                        .then(arenas(extension)))
                .and(literal("powerupscenter")
                        .then(arenas(extension)))
                .and(literal("joinguiitem")
                        .then(arenas(extension)))
                .and(literal("regenerate")
                        .then(arenas(extension)))
                .and(literal("spectatingpoint")
                        .then(arenas(extension)))
                .and(literal("sign")
                        .then(arenas(extension)))
                .and(literal("removespectatingpoint")
                        .then(arenas(extension)))
                .and(literal("finishingloc")
                        .then(arenas(extension)))
                .and(literal("removefinishingloc")
                        .then(arenas(extension)))
                .and(list("lobby", "removelobby")
                        .then(arenas(extension)
                                .then(indexesAndTeams(args, 1))))
                .then(literal("spawnpoint")
                        .then(arenas(extension)
                                .then(indexesAndTeams(args, 1))
                                .and(literal("all"))))
                .then(literal("settings")
                        .then(arenas(extension)
                                .then(list("deathLevel", "disable", "displayName", "enable", "gameTime", "powerupsRadius", "maxPlayerCount", "membersPerTeam", "minimum", "teams", "toggle"))));
    }


}