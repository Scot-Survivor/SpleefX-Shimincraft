package net.spleefx.gui;

import com.cryptomorin.xseries.XMaterial;
import net.spleefx.arena.Arenas;
import net.spleefx.arena.MatchArena;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.arena.type.spleef.SpleefArena;
import net.spleefx.arena.type.splegg.SpleggArena;
import net.spleefx.model.Item;
import net.spleefx.util.Metadata;
import net.spleefx.util.game.Chat;
import net.spleefx.util.menu.BooleanButton;
import net.spleefx.util.menu.Button;
import net.spleefx.util.menu.InventoryUI;
import net.spleefx.util.menu.NumberButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.stream.IntStream;

public class ArenaSettingsUI extends InventoryUI {

    public static final Metadata<MatchArena> RENAMING = Metadata.of("renaming");

    private static final NumberButton DEATH_LEVEL = NumberButton.builder()
            .item(Item.builder().type(Material.REDSTONE)
                    .name("&cDeath Level")
                    .loreV("&aThe Y value in which if players reach, they lose")
                    .build())
            .minimum(-100)
            .build();

    private static final NumberButton MINIMUM = NumberButton.builder()
            .item(Item.builder().type(XMaterial.HOPPER)
                    .name("&eMinimum players required")
                    .loreV("&aMinimum amount of players required for the game to start")
                    .build())
            .minimum(2)
            .build();

    private static final NumberButton MAXIMUM = NumberButton.builder()
            .item(Item.builder().type(Material.IRON_HELMET)
                    .name("&eMaximum player count")
                    .loreV("&aThe maximum amount of players in the arena")
                    .build())
            .minimum(2)
            .build();

    private static final NumberButton PER_TEAM = NumberButton.builder()
            .item(Item.builder().type(XMaterial.IRON_HELMET)
                    .name("&eMembers per team")
                    .loreV("&aHow many people in each team")
                    .build())
            .minimum(2)
            .build();

    private static final NumberButton GAME_TIME = NumberButton.builder()
            .item(Item.builder().type(XMaterial.CLOCK)
                    .name("&eGame time")
                    .loreV("&aHow many minutes the game can last for")
                    .build())
            .build();

    private static final NumberButton POWERUP_RADIUS = NumberButton.builder()
            .item(Item.builder().type(XMaterial.FIREWORK_ROCKET)
                    .name("&ePower-up spawn radius")
                    .loreV("&aThe radius of spawning power-ups", "&aaround the arena's center")
                    .build())
            .build();

    private static final BooleanButton DROP_MINED_BLOCKS = new BooleanButton(Item.builder().type(Material.BRICK)
            .name("&eDrop mined blocks")
            .lore("&aShould mined blocks have their standard drops dropped")
            .build());

    private static final BooleanButton MELTING = new BooleanButton(Item.builder().type(XMaterial.LAVA_BUCKET)
            .name("&eMelting")
            .lore("&aShould snow melt around players when they are not moving")
            .build());

    private static final BooleanButton DESTROYABLE = new BooleanButton(Item.builder().type(XMaterial.ANVIL)
            .name("&eAll blocks destroyable by default")
            .lore("&aWhether are all blocks destroyable by default.", "", "&7If &aenabled&7, blocks in /splegg materials will be ",
                    "&enon-destroyable&7.", "", "&7If &cdisabled&7, blocks in /splegg materials will be", "&edestroyable", "", "&a/splegg materials")
            .build());

    public static final Item RENAME_ARENA = Item.builder().type(XMaterial.NAME_TAG)
            .name("&eRename Arena")
            .build();

    private static final Item DELETE = Item.builder().type(Material.TNT)
            .name("&cDelete arena")
            .lore("&aDelete the arena and all its data")
            .build();

    private static final Item BARRIER = Item.builder().type(XMaterial.BARRIER)
            .name("&eNo teams in this arena!")
            .build();

    public ArenaSettingsUI(MatchArena arena, Player sender) {
        super("&1Settings for " + arena.getDisplayName(), 6);
        cancelAllClicks = true;
        DEATH_LEVEL.addTo(this, 36, arena::getDeathLevel, arena::setDeathLevel);
        if (arena.isFFA())
            MAXIMUM.addTo(this, 37, arena::getMaxPlayerCount, arena::setMaxPlayerCount);
        else
            PER_TEAM.addTo(this, 37, arena::getMembersPerTeam, arena::setMembersPerTeam);
        GAME_TIME.addTo(this, 38, arena::getGameTime, arena::setGameTime);
        MINIMUM.addTo(this, 39, arena::getMinimum, arena::setMinimum);
        DROP_MINED_BLOCKS.addTo(this, 33, arena::isDropMinedBlocks, arena::setDropMinedBlocks);
        if (arena instanceof SpleefArena) {
            SpleefArena spleef = (SpleefArena) arena;
            MELTING.addTo(this, 34, spleef::isMelt, spleef::setMelt);
        } else if (arena instanceof SpleggArena) {
            SpleggArena splegg = (SpleggArena) arena;
            DESTROYABLE.addTo(this, 34, splegg::isDestroyableByDefault, splegg::setDestroyableByDefault);
        }
        POWERUP_RADIUS.addTo(this, 40, arena::getPowerupsRadius, arena::setPowerupsRadius);
        register(16, Button.builder().close().cancelClick().item(RENAME_ARENA).handle((e) -> {
            RENAMING.set(e.getWhoClicked(), arena);
            Chat.plugin(e.getWhoClicked(), "&eType in the display name for arena &d" + arena.getKey() + "&e.");
            Chat.plugin(e.getWhoClicked(), "&eTo cancel, type &dcancel-edit&e.");
        }).build());

        register(53, Button.builder().close().cancelClick().item(DELETE).handle((e) ->
                Arenas.deleteArena(arena)
        ).build());

        if (arena.isFFA()) {
            IntStream.range(0, 6).forEach(i -> register(i, Button.plain(BARRIER)));
            IntStream.range(9, 15).forEach(i -> register(i, Button.plain(BARRIER)));
        } else {
            int i = 0;
            for (MatchTeam team : MatchTeam.teams()) {
                if (i > 6) break;
                new BooleanButton(Item.team(team)).addTo(
                        this,
                        i,
                        arena.getTeams().contains(team),
                        BooleanButton.sync(arena.getTeams(), team)
                );
                ++i;
            }
        }
        whenClosed(c -> arena.getSignHandler().update());
        display(sender);
    }
}
