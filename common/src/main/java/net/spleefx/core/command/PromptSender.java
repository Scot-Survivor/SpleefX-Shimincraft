package net.spleefx.core.command;

import net.spleefx.arena.MatchArena;
import net.spleefx.arena.engine.ReloadedArenaEngine;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.extension.MatchExtension;
import net.spleefx.util.game.Chat;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.message.message.Message;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.spleefx.backend.Schedulers.sneakyThrow;

/**
 * A lightweight wrapper for {@link CommandSender} with helping methods
 * to reduce the code
 */
public class PromptSender {

    private static final UUID CONSOLE_UUID = new UUID(0, 0); // 00000000-0000-0000-0000-000000000000
    private static final Map<UUID, PromptSender> SENDERS = Collections.synchronizedMap(new HashMap<>());

    public final CommandSender sender;

    public PromptSender(CommandSender sender) {
        this.sender = sender;
    }

    public String name() {
        return sender.getName();
    }

    public void reply(@Nullable Prefixable prefix, String message, Object... format) {
        if (prefix == null) prefix = Prefixable.PLUGIN;
        sender.sendMessage(Chat.colorize(prefix.getPrefix() + String.format(message, format)));
    }

    public void reply(@Nullable ComponentJSON jsonMessage) {
        if (jsonMessage == null) return;
        PluginCompatibility.send(jsonMessage, sender);
    }

    public Player player() throws CommandException {
        if (!isPlayer())
            throw new CommandException(Message.NOT_PLAYER.create());
        return (Player) sender;
    }

    public Location location() throws CommandException {
        return player().getLocation();
    }

    public MatchPlayer arenaPlayer() throws CommandException {
        return MatchPlayer.wrap(player());
    }

    public boolean isOp() {
        return sender.isOp();
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    public MatchArena arena() throws CommandException {
        MatchArena arena = arenaPlayer().getArena();
        if (arena == null)
            throw new CommandException(Message.MUST_BE_IN_ARENA.create());
        return arena;
    }

    public ReloadedArenaEngine engine() throws CommandException {
        return arena().getEngine();
    }

    public ItemStack getMainHand() {
        return PluginCompatibility.attempt(() -> {
            try {
                return player().getInventory().getItemInMainHand();
            } catch (CommandException e) {
                sneakyThrow(e);
                return null;
            }
        }, () -> {
            try {
                return player().getItemInHand();
            } catch (CommandException e) {
                sneakyThrow(e);
                return null;
            }
        });
    }

    /**
     * Returns the extension of the arena they are in
     *
     * @return The extension
     */
    public MatchExtension ext() throws CommandException {
        return arena().getExtension();
    }

    public void gui(Inventory inventory) throws CommandException {
        player().openInventory(inventory);
    }

    public void gui(InventoryUI menu) throws CommandException {
        menu.display(player());
    }

    public void reply(String message, Object... format) {
        reply(null, message, format);
    }

    public static PromptSender adapt(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender)
            return SENDERS.computeIfAbsent(CONSOLE_UUID, c -> new PromptSender(sender));
        return SENDERS.computeIfAbsent(((Entity) sender).getUniqueId(), c -> new PromptSender(sender));
    }

    public static void unregister(UUID uuid) {
        SENDERS.remove(uuid);
    }

}