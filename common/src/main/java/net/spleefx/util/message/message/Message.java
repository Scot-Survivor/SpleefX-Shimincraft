package net.spleefx.util.message.message;

import lombok.EqualsAndHashCode;
import net.spleefx.SpleefX;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.core.command.Prefixable;
import net.spleefx.util.Placeholders;
import net.spleefx.util.game.Chat;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents a message. Construct with {@link MessageBuilder}.
 */
@EqualsAndHashCode
public class Message implements Iterable<Message> {

    public static final Message PREFIX = new MessageBuilder("Prefix")
            .describe("The prefix that comes before any message. To send a message without prefix, precede it with '[noprefix]'.")
            .defaultTo("&7[&bSpleef&fX&7] ").build();

    public static final Message ARENA_CREATED = new MessageBuilder("Arena.Created")
            .describe("Sent when an arena is created")
            .defaultTo("&aArena &e{arena} &ahas been created. Run &e/{command} arena settings {arena}&a.")
            .build();

    public static final Message ARENA_DELETED = new MessageBuilder("Arena.Deleted")
            .describe("Sent when an arena has been deleted")
            .defaultTo("&aArena &e{arena} &ahas been deleted")
            .build();

    public static final Message SPAWNPOINT_SET = new MessageBuilder("Arena.SpawnpointSet")
            .describe("Sent when a team spawnpoint has been set")
            .defaultTo("&aTeam {team}&a's spawnpoint has been set to &e{x}&a, &e{y}&a, &e{z}&a.")
            .build();

    public static final Message LOBBY_SET = new MessageBuilder("Arena.LobbySet")
            .describe("Sent when a lobby is set")
            .defaultTo("&aArena &e{arena}&a's lobby has been set to &e{x}&a, &e{y}&a, &e{z}&a.")
            .build();

    public static final Message NO_PERMISSION = new MessageBuilder("Command.NoPermission")
            .describe("Sent when a player attempts to execute a command but has no permission")
            .defaultTo("&cYou do not have permission to perform this command!")
            .build();

    public static final Message NOT_PLAYER = new MessageBuilder("Command.NotPlayer")
            .describe("Sent when a command sender is not a player when required")
            .defaultTo("&cYou must be a player to use this command!")
            .build();

    public static final Message UNKNOWN_PLAYER = new MessageBuilder("Command.InvalidPlayer")
            .describe("Sent when the inputted player is either offline or invalid")
            .defaultTo("&cPlayer &e{player} &cis offline or invalid.")
            .build();

    public static final Message UNKNOWN_SUBCOMMAND = new MessageBuilder("Command.InvalidSubcommand")
            .describe("Sent when a player runs an unknown subcommand")
            .defaultTo("&cUnrecognizable subcommand. Try &e/{command} help&c.")
            .build();

    public static final Message TEAM_NOT_REGISTERED = new MessageBuilder("Command.TeamNotRegistered")
            .describe("Sent when attempting to set a spawnpoint for an invalid team")
            .defaultTo("&cTeam {team} &cis not registered in arena &e{arena}&c. Add the team in &e/spleef arena settings {arena}&c.")
            .build();

    public static final Message INVALID_USAGE = new MessageBuilder("Command.InvalidUsage")
            .describe("Sent when a command is executed with an invalid count of parameters")
            .defaultTo("&cInvalid usage. Try &e/{command} &b{command_usage}&c.")
            .build();

    public static final Message MUST_BE_IN_ARENA = new MessageBuilder("Command.MustBeInArena")
            .describe("Sent when the player attempts to execute a command that only works when they are in an arena")
            .defaultTo("&cYou must be in an arena to use this command!")
            .build();

    public static final Message DISALLOWED_COMMAND = new MessageBuilder("Game.CommandNotAllowed")
            .describe("Sent when a player attempts to send a disallowed command in-game")
            .defaultTo("&cYou may not execute this command while in-game!")
            .build();

    public static final Message NO_AVAILABLE_ARENA = new MessageBuilder("PlayerCannotJoin.NoAvailableArenas")
            .describe("Sent when attempting to pick a random arena but none is found")
            .defaultTo("&cI couldn't find an available arena for you")
            .build();

    public static final Message LEFT_THE_ARENA = new MessageBuilder("Command.LeftArena")
            .describe("Sent when a player leaves their arena")
            .defaultTo("&cYou left the arena.")
            .build();

    public static final Message LEFT_THE_ARENA_AND_LOST = new MessageBuilder("Command.LeftArenaAndLost")
            .describe("Sent when a player leaves their arena and loses")
            .defaultTo("&cYou left the arena and lost.")
            .build();

    public static final Message NO_ARENAS = new MessageBuilder("Command.NoArenasInMode")
            .describe("Sent in /<mode> listarenas when there are not any arenas")
            .defaultTo("&cThis game type has no arenas!")
            .build();

    public static final Message ARENA_REGENERATING = new MessageBuilder("PlayerCannotJoin.ArenaRegenerating")
            .describe("Sent when a player attempts to join a regenerating arena")
            .defaultTo("&cPlease wait while this arena regenerates.")
            .build();

    public static final Message ARENA_NEEDS_SETUP = new MessageBuilder("PlayerCannotJoin.ArenaNotReady")
            .describe("Sent when a player attempts to join an unplayable arena")
            .defaultTo("&cThis arena is not in a playable state.")
            .build();

    public static final Message ARENA_ALREADY_ACTIVE = new MessageBuilder("PlayerCannotJoin.ArenaAlreadyActive")
            .describe("Sent when a pleyer attempts to join an active arena")
            .defaultTo("&cThis arena is already active!")
            .build();

    public static final Message MUST_WAIT_BEFORE_JOINING = new MessageBuilder("PlayerCannotJoin.MustWait")
            .describe("Sent when a pleyer attempts to join an arena but are on cooldown")
            .defaultTo("&cYou must wait before joining again!")
            .build();

    public static final Message ARENA_FULL = new MessageBuilder("PlayerCannotJoin.ArenaFull")
            .describe("Sent when a pleyer attempts to join a full arena")
            .defaultTo("&cThis arena is full!")
            .build();

    public static final Message ARENA_HAS_NO_PLAYERS = new MessageBuilder("PlayerCannotJoin.ArenaHasNoPlayers")
            .describe("Sent when a pleyer attempts to spectate an arena but it has no players")
            .defaultTo("&cNo players to spectate!")
            .build();

    public static final Message ARENA_DISABLED = new MessageBuilder("PlayerCannotJoin.ArenaDisabled")
            .describe("Sent when a player attempts to join a disabled arena")
            .defaultTo("&cThis arena is disabled")
            .build();

    public static final Message MUST_HAVE_EMPTY_INV = new MessageBuilder("PlayerCannotJoin.MustHaveEmptyInventory")
            .describe("Sent when a player attempts to join with a non-empty inventory when it is required.")
            .defaultTo("&cYou must have an empty inventory in order to join!")
            .build();

    public static final Message ARENA_ALREADY_EXISTS = new MessageBuilder("Arena.AlreadyExists")
            .describe("Sent when attempting to build an arena with an existing key")
            .defaultTo("&cAn arena with the key &e{arena} &calready exists! Remove it with &e/spleef arena remove {arena}&c.")
            .build();

    public static final Message CLICK_TO_GO_TO_SX_NET = new MessageBuilder("Arena.GameSummaryTooltip")
            .defaultTo("Click to be taken to &bhttps://spleefx.net&f.")
            .describe("Appears on the URL tooltip when generating game summary")
            .build();

    public static final Message INVALID_ARENA = new MessageBuilder("Command.InvalidArena")
            .describe("Sent when the requested arena is invalid")
            .defaultTo("&cNo arena with key &e{arena} &cexists!")
            .build();

    public static final Message SERVER_STOPPED = new MessageBuilder("Game.ForciblyEndingServerStopped")
            .describe("Sent to in-game players when the server stops")
            .defaultTo("&cThe server has stopped, so the game has been forcibly ended.")
            .build();

    public static final Message NOT_ENOUGH_PLAYERS = new MessageBuilder("Game.CountdownCancelledNotEnoughPlayers")
            .describe("Broadcasted when a player leaves and the minimum player count is not met")
            .defaultTo("&eThere are not enough players to start the game. Countdown cancelled.")
            .build();

    public static final Message GAME_COUNTDOWN = new MessageBuilder("Game.Countdown")
            .describe("Broadcasted when an arena is counting down")
            .defaultTo("&aGame starting in &e{colored_number} &asecond{plural}")
            .build();

    public static final Message GAME_TIMEOUT = new MessageBuilder("Game.TimeOver")
            .describe("Broadcasted when the game is about to time out")
            .defaultTo("&7The game ends in {colored_number} &7second{plural}")
            .build();

    public static final Message GAME_STARTING = new MessageBuilder("Game.Starting")
            .describe("Broadcasted when there are enough players to start the game")
            .defaultTo("&aThere are enough players to start the game. Starting in &e{value} second{plural}&a.")
            .build();

    public static final Message ALREADY_IN_ARENA = new MessageBuilder("Arena.CannotJoinAlreadyInArena")
            .describe("Sent when the player attempts to join an arena but is in one already.")
            .defaultTo("&cYou are already in an arena!")
            .build();

    public static final Message GRACE_PERIOD_START = new MessageBuilder("Arena.GracePeriodStarted")
            .describe("Broadcasted when the grace period starts")
            .defaultTo("&7The grace period has started! You have &e{time} to spread out!")
            .build();

    public static final Message GRACE_PERIOD_ENDED = new MessageBuilder("Arena.GracePeriodEnded")
            .describe("Broadcasted when the grace period ends")
            .defaultTo("&cThe grace period is over!")
            .build();

    public static final Message PLAYER_JOINED_T = new MessageBuilder("Game.PlayerJoined")
            .describe("Broadcasted when a player joins the arena")
            .defaultTo("&e{player} &ahas joined the game - {team} Team&a! &7(&9{arena_playercount}&c/&9{arena_maximum}&7)")
            .build();

    public static final Message TEAM_ELIMINATED = new MessageBuilder("Game.TeamEliminated")
            .describe("Broadcasted when a team has been eliminated")
            .defaultTo("&eTeam {team} &ehas been eliminated!")
            .build();

    public static final Message PLAYER_LOST_T = new MessageBuilder("Game.PlayerLost")
            .describe("Broadcasted when a player loses")
            .defaultTo("&c{team_color}{player} &chas been eliminated!")
            .build();

    public static final Message PLAYER_WINS_T = new MessageBuilder("Game.PlayerWins")
            .describe("Broadcasted when a player wins in a teams arena")
            .defaultTo("{team_color}{player} &ahas won!")
            .build();

    public static final Message PLAYER_JOINED_FFA = new MessageBuilder("Game.PlayerJoinedFFA")
            .describe("Broadcasted when a player joins the arena")
            .defaultTo("&e{player} &ahas joined the game! &7(&9{arena_playercount}&c/&9{arena_maximum}&7)")
            .build();

    public static final Message PLAYER_LOST_FFA = new MessageBuilder("Game.PlayerLostFFA")
            .describe("Broadcasted when a player loses")
            .defaultTo("&c{player} &chas been eliminated!")
            .build();

    public static final Message PLAYER_WINS_FFA = new MessageBuilder("Game.PlayerWonFFA")
            .describe("Broadcasted when a player wins")
            .defaultTo("{player} &ahas won!")
            .build();

    public static final Message GAME_SUMMARY = new MessageBuilder("Game.GameSummaryLink")
            .describe("Sent to players to include the game summary URL")
            .defaultTo("&bClick here to view your game statistics!")
            .build();

    public static final Message POWER_UP_DELAY = new MessageBuilder("Game.PowerupDelay")
            .describe("Sent when a player has to wait before taking another power-up")
            .defaultTo("&cYou must wait before taking another power-up!")
            .build();

    public static final Message WAITING = new MessageBuilder("Stage.Waiting")
            .describe("Displayed on sign when an arena is waiting for players")
            .defaultTo("&bWaiting")
            .build();

    public static final Message COUNTDOWN = new MessageBuilder("Stage.Countdown")
            .describe("Displayed on sign when an arena is counting down to start")
            .defaultTo("&1Starting")
            .build();

    public static final Message ACTIVE = new MessageBuilder("Stage.Active")
            .describe("Displayed on sign when an arena is active")
            .defaultTo("&4Running")
            .build();

    public static final Message REGENERATING = new MessageBuilder("Stage.Regenerating")
            .describe("Displayed on sign when an arena is regenerating")
            .defaultTo("&2Regenerating")
            .build();

    public static final Message NEEDS_SETUP = new MessageBuilder("Stage.NeedsSetup")
            .describe("Displayed on sign when an arena has not been fully setup")
            .defaultTo("&cNeeds setup")
            .build();

    public static final Message DISABLED = new MessageBuilder("Stage.Disabled")
            .describe("Displayed on sign when a mode is disabled")
            .defaultTo("&cDisabled")
            .build();

    public static final Message UPGRADE_SELECTED = new MessageBuilder("SpleggUpgrades.UpgradeSelected")
            .describe("Sent when a player selects a splegg upgrade.")
            .defaultTo("&aYou have selected &e{upgrade_displayname}&a.")
            .build();

    public static final Message NOT_ENOUGH_COINS_SPLEGG = new MessageBuilder("SpleggUpgrades.NotEnoughCoins")
            .describe("Sent when a player tries to purchase an upgrade but does not have enough coins.")
            .defaultTo("&cYou do not have enough coins to purchase this upgrade!")
            .build();

    public static final Message UPGRADE_PURCHASED = new MessageBuilder("SpleggUpgrade.UpgradePurchased")
            .describe("Sent when a player successfully purchases a splegg upgrade.")
            .defaultTo("&aSuccessfully purchased and selected &e{upgrade_displayname}&a!")
            .build();

    public static final Message MUST_PURCHASE_BEFORE = new MessageBuilder("SpleggUpgrade.MustPurchaseBefore")
            .describe("Sent when a player tries to purchase an upgrade but hasn't unlocked the ones required first.")
            .defaultTo("&cYou must purchase previous abilities before buying this!")
            .build();

    public static final Message SPLEGG_GUI_SELECTED = new MessageBuilder("SpleggGUI.Selected")
            .describe("Displayed on the splegg upgrades GUI when an upgrade is selected")
            .defaultTo("&aSelected!")
            .build();

    public static final Message SPLEGG_GUI_CLICK_TO_SELECT = new MessageBuilder("SpleggGUI.ClickToSelect")
            .describe("Displayed on the splegg upgrades GUI when an upgrade is purchased but not selected")
            .defaultTo("&aClick to select")
            .build();

    public static final Message SPLEGG_GUI_CLICK_TO_PURCHASE = new MessageBuilder("SpleggGUI.ClickToPurchase")
            .describe("Displayed on the splegg upgrades GUI when an upgrade is purchased but not selected")
            .defaultTo("&eClick to purchase")
            .build();

    public static final Message SPLEGG_GUI_NOT_ENOUGH_COINS = new MessageBuilder("SpleggGUI.NotEnoughCoins")
            .describe("Displayed on the splegg upgrades GUI when an upgrade is not purchased and the user cannot select it")
            .defaultTo("&cYou do not have enough coins")
            .build();

    public static final Message PARTY_NOT_ENOUGH_SPACE = new MessageBuilder("Party.NotEnoughSpace")
            .describe("Sent when a party leader attempts to join a game with a party but there isn't enough space for the party")
            .defaultTo("&cThere isn't enough space for your party!")
            .build();

    public static final Message PARTY_MEMBERS_IN_GAME = new MessageBuilder("Party.MembersInGame")
            .describe("Sent when a party leader attempts to join a game with a party but there are players who are in a game")
            .defaultTo("&cThere are members of your party who are in a game!")
            .build();

    public static final Message PARTY_NOT_LEADER = new MessageBuilder("Party.NotLeader")
            .describe("Sent when a party member attempts to join a game but they are not a leader")
            .defaultTo("&cOnly the party leader can join games!")
            .build();

//    public static final Message TEAM_FULL = new MessageBuilder("TeamSelection.TeamFull")
//            .describe("Sent to the player when they attempt to join a full team through the team selection menu.")
//            .defaultTo("&cThis team is full!").build();
//
//    public static final Message ALREADY_IN_THIS_TEAM = new MessageBuilder("TeamSelection.AlreadyInThisTeam")
//            .describe("Sent to the player when they attempt to join a team but they already are in it.")
//            .defaultTo("&cYou are already in this team!").build();
//
//    public static final Message JOINED_TEAM = new MessageBuilder("TeamSelection.JoinedTeam")
//            .describe("Sent to the player when they join a specific team.")
//            .defaultTo("&aYou joined team {team}&a.").build();

    private final String key;
    private final String defaultValue;
    private final String comment;
    private final String[] description;

    private String value;

    Message(String key, String defaultValue, String comment, String[] description) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.description = description;
        SpleefX.getSpleefX().getMessageManager().registerMessage(this);
    }

    public String build(boolean flatten, Object... formats) {
        String value = getValue();
        if (!value.contains("[noprefix]")) {
            for (Object f : formats) {
                if (f instanceof Prefixable) {
                    value = "[noprefix]" + ((Prefixable) f).getPrefix() + value;
                    break;
                }
            }
        }
        return Placeholders.on(value, flatten ? flatten(formats).toArray() : formats);
    }

    public @NotNull String create(Object... formats) {
        return build(true, formats);
    }

    /**
     * Flattens the specified array by joining all nested arrays to a single array.
     *
     * @param array The array to flatten
     * @return A stream of the flattened array.
     */
    public static Stream<Object> flatten(Object[] array) {
        return Arrays.stream(array).flatMap(o -> o instanceof Object[] ? flatten((Object[]) o) : Stream.of(o));
    }

    public String getValue() {
        return value == null ? value = defaultValue : value;
    }

    public String getKey() {
        return key;
    }

    public String getComment() {
        return comment;
    }

    public String[] getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void reply(CommandSender cs, Object... formats) {
        reply(true, cs, formats);
    }

    public void reply(MatchPlayer cs, Object... formats) {
        reply(true, cs.player(), formats);
    }

    public void reply(boolean flatten, CommandSender cs, Object... formats) {
        if (getValue().equals("{}")) return;
        String text = build(flatten, flatten(formats).toArray());
        if (text.contains("[noprefix]"))
            text = text.replace("[noprefix]", "");
        else
            text = PREFIX.getValue() + text;
        Chat.sendUnprefixed(cs, text);
    }

    private static final Set<Message> MESSAGES = new HashSet<>();

    public static void load() {
        for (Field field : Message.class.getDeclaredFields()) {
            if (Message.class.isAssignableFrom(field.getType())) {
                try {
                    Message message = (Message) field.get(null);
                    MESSAGES.add(message);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override public String toString() {
        return create();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override public Iterator<Message> iterator() {
        return all();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    public static Iterator<Message> all() {
        return MESSAGES.iterator();
    }

}
