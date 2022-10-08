package net.spleefx.core.command;

import com.google.common.collect.ImmutableList;
import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.core.command.permission.PermissionSupplier;
import net.spleefx.util.game.Chat;
import net.spleefx.util.paginate.ListPaginator;
import net.spleefx.util.paginate.ListPaginator.Header;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * Represents information about a specific command.
 */
public class CommandMeta {

    public final String name, description, parameters;
    public final PermissionSupplier permission;
    public final boolean requirePlayer, requireArena, requireNotInArena, extensionCommand;
    public final IntPredicate argumentsValidation;
    public final List<String> helpMenu;
    public final List<String> aliases;
    public final boolean registerBoth;

    public CommandMeta(String name, String description, String parameters, PermissionSupplier permission, boolean requirePlayer, boolean requireArena, boolean requireNotInArena, boolean extensionCommand, IntPredicate argumentsValidation, List<String> helpMenu, List<String> aliases, boolean registerBoth) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.permission = permission;
        this.requirePlayer = requirePlayer;
        this.requireArena = requireArena;
        this.requireNotInArena = requireNotInArena;
        this.extensionCommand = extensionCommand;
        this.argumentsValidation = argumentsValidation;
        this.helpMenu = helpMenu;
        this.aliases = aliases;
        this.registerBoth = registerBoth;
    }

    public final ComponentJSON helpJSON = new ComponentJSON();

    public static final Header HEADER = (sender, pageIndex, pageCount) ->
            Chat.sendUnprefixed(sender, "&c&m---------&r&a (&b" + (pageIndex + " &e/&c " + pageCount) + "&a) &c&m---------");

    public final ListPaginator<String, ChatComponent> helpPaginator = new ListPaginator<>(
            7,
            (sender, json) -> helpJSON.clear().append(json).send(sender).clear(),
            (a) -> new ChatComponent().setText(a));

    {
        helpPaginator.setHeader(HEADER);
    }

    public static Builder of(@NotNull String name, String... aliases) {
        return new Builder(name, aliases);
    }

    public static class Builder {

        private final String name;
        private String description;
        private String parameters = "";
        private String permission;
        private PermissionDefault permissionAccess;

        private List<String> aliases;
        private boolean requirePlayer = false;
        private boolean requireArena = false;
        private boolean requireNotInArena = false;
        private boolean extensionCommand = false;
        private boolean registerBoth = false;
        private IntPredicate argsValidation = ArgumentPredicates.anything();
        private List<String> helpMenu = new ArrayList<>();

        protected Builder(@NotNull String name, String... aliases) {
            this.name = n(name, "name");
            this.aliases = Arrays.asList(aliases);
        }

        public Builder description(@NotNull String description) {
            this.description = n(description, "description");
            return this;
        }

        public Builder parameters(@NotNull String parameters) {
            this.parameters = n(parameters, "parameters");
            return this;
        }

        public Builder permission(@NotNull String defaultNode) {
            return permission(defaultNode, PermissionDefault.OP);
        }

        public Builder permission(@NotNull String defaultNode, PermissionDefault access) {
            permission = defaultNode;
            permissionAccess = access;
            return this;
        }

        public Builder requirePlayer() {
            requirePlayer = true;
            return this;
        }

        public Builder requireArena() {
            requirePlayer();
            requireArena = true;
            return this;
        }

        public Builder requireNotInArena() {
            requirePlayer();
            requireNotInArena = true;
            return this;
        }

        public Builder extensionCommand() {
            extensionCommand = true;
            return this;
        }

        public Builder registerBoth() {
            registerBoth = true;
            return this;
        }

        public Builder withHelpMenu(String... menu) {
            helpMenu = new ArrayList<>(Arrays.asList(menu));
            helpMenu.replaceAll(s -> {
                s = (s == null) ? "" : s;
                return s.replace("- ", "&7-&b ");
            });
            return this;
        }

        public Builder checkIfArgsAre(IntPredicate predicate) {
            argsValidation = predicate;
            return this;
        }

        public CommandMeta build() {
            return new CommandMeta(
                    name,
                    description,
                    parameters,
                    permission == null ? PermissionSupplier.none() : extensionCommand ?
                            PermissionSupplier.fromDynamic(permission, permissionAccess) :
                            PermissionSupplier.fromStatic(permission, permissionAccess),
                    requirePlayer,
                    requireArena,
                    requireNotInArena,
                    extensionCommand,
                    argsValidation,
                    ImmutableList.copyOf(helpMenu),
                    ImmutableList.copyOf(aliases),
                    registerBoth
            );
        }

        private static <T> T n(T value, String err) {
            Objects.requireNonNull(value, err);
            return value;
        }
    }

}