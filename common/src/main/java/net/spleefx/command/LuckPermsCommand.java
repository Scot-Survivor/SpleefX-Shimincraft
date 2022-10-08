package net.spleefx.command;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.PermissionNode;
import net.spleefx.SpleefX;
import net.spleefx.annotation.RegisteredCommand;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.*;
import net.spleefx.core.command.tab.RootNode;
import net.spleefx.core.command.tab.TabCompletion;
import net.spleefx.extension.Extensions;
import net.spleefx.extension.MatchExtension;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static net.spleefx.core.command.tab.TabCompletion.arguments;

@RegisteredCommand
public class LuckPermsCommand extends BaseCommand {

    private final boolean luckPermsPresent = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");

    @Override protected @NotNull CommandMeta getCommandMeta() {
        return CommandMeta.of("luckperms", "lp")
                .parameters("<group to add permissions to>")
                .description("Add all player-only permissions in SpleefX to the specified rank.")
                .checkIfArgsAre(atLeast(1))
                .permission("spleefx.luckperms")
                .build();
    }

    @Override public @NotNull Response execute(@NotNull SpleefX plugin, @NotNull PromptSender sender, @NotNull CommandArgs args, @Nullable MatchExtension _ex) throws CommandException {
        if (!luckPermsPresent) {
            new ComponentJSON().append(Mson.prefixed("&cYou must have &eLuckPerms &cto use this command!")
                    .url("https://www.spigotmc.org/resources/luckperms.28140/")
                    .tooltip("Click here to view LuckPerms's page")).send(sender.sender);
            return Response.error();
        }

        LuckPerms luckPerms = LuckPermsProvider.get();
        Group group = luckPerms.getGroupManager().getGroup(args.get(0));
        if (group == null) {
            return Response.error("&cThis group does not exist!");
        }

        NodeMap nodeMap = group.data();
        Set<String> added = new LinkedHashSet<>();

        commandHandler.getCommands(null).values().stream()
                .filter(c -> c.meta.permission.isDefault())
                .filter(c -> c.meta.permission.node() != null)
                .map(c -> PermissionNode.builder(c.meta.permission.node()).build())
                .peek(nodeMap::add)
                .forEach(node -> added.add(node.getPermission()));

        for (MatchExtension extension : Extensions.getExtensions()) {
            commandHandler.getCommands(extension).values().stream()
                    .filter(c -> c.meta.permission.isDefault())
                    .map(c -> PermissionNode.builder(c.meta.permission.getPermission(extension).getName()).build())
                    .peek(nodeMap::add)
                    .forEach(node -> added.add(node.getPermission()));
        }
        StringJoiner modifications = new StringJoiner("\n");
        added.forEach(c -> modifications.add("&b" + c));

        luckPerms.getGroupManager().saveGroup(group);
        new ComponentJSON().append(Mson.prefixed("&aSuccessfully added &e%s &apermissions to group &b%s&a. " +
                "&bHover to view the permissions added.", added.size(), group.getName())
                .tooltip(modifications.toString()))
                .send(sender.sender);
        return Response.ok();
    }

    @Override public @Nullable RootNode onTab(@Nullable MatchExtension extension, Command command, PromptSender sender, String[] args) {
        if (!luckPermsPresent)
            return null;
        LuckPerms api = LuckPermsProvider.get();
        return TabCompletion.of(arguments(api.getGroupManager().getLoadedGroups().stream().map(Group::getName).collect(Collectors.toList())));
    }
}
