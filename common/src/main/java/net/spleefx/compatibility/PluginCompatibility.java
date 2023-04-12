package net.spleefx.compatibility;

import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.compatibility.packet.ChatPacket;
import net.spleefx.hook.parties.PartyHook;
import net.spleefx.hook.worldguard.WorldGuardHook;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.spleefx.util.Placeholders.firstNotNull;

@SuppressWarnings("Convert2MethodRef")
public abstract class PluginCompatibility {

    public static final AtomicBoolean DISABLE = new AtomicBoolean();
    public static final AtomicBoolean MISSING_WE = new AtomicBoolean();

    private static ProtocolNMS NMS;
    private static WorldGuardHook WG;
    private static PartyHook PARTIES;

    public static <T> T create(@NotNull String name, Supplier<@Nullable T> fallback) {
        try {
            name = name.replace("$version", Protocol.VERSION).replace("$lm", Protocol.supports(13) ? "modern" : "legacy");
            Class<? extends T> type = (Class<? extends T>) Class.forName(name.replace("$version", Protocol.VERSION));
            return type.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return fallback.get();
        }
    }

    public static <T> T attempt(Supplier<T> first, Supplier<T> second) {
        try {
            return first.get();
        } catch (Throwable t) {
            return second.get();
        }
    }

    public static void load() {
        NMS = create("net.spleefx.$version.ProtocolNMSImpl", GenericProtocolNMS::new);
        try {
            firstNotNull(
                    Bukkit.getPluginManager().getPlugin("WorldEdit"),
                    Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit"),
                    Bukkit.getPluginManager().getPlugin("AsyncWorldEdit")
            );
        } catch (Throwable t) {
            MISSING_WE.set(true);
        }

        WorldGuardHook wg = WorldGuardHook.FALLBACK;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
            wg = create("net.spleefx.$lm.DefaultWorldGuardHook", () -> WorldGuardHook.FALLBACK);
        WG = wg;
        PARTIES = PartyHook.findHook();
    }

    public static void send(ComponentJSON component, CommandSender player) {
        if (!(player instanceof Player)) return;
        ChatPacket packet = new ChatPacket().setMessage(component).setChatType(ChatType.SYSTEM);
        packet.sendPacket((Player) player);
    }

    public static void hidePlayer(Player toHide, Player target) {
        NMS.hidePlayer(toHide, target);
    }

    public static void showPlayer(Player toHide, Player target) {
        NMS.showPlayer(toHide, target);
    }

    public static void setCollidable(Player player, boolean newValue) {
        NMS.setCollidable(player, newValue);
    }

    public static Entity getEntity(UUID uuid) {
        return NMS.getEntity(uuid);
    }

    public static void setUnbreakable(ItemMeta item) {
        NMS.setUnbreakable(item);
    }

    public static double getMaxHealth(Player player) {
        return NMS.getMaxHealth(player);
    }

    public static <E extends Entity> E getEnt(UUID uuid) {
        return ProtocolNMS.getEnt(uuid);
    }

    public static ProtocolNMS getNMS() {
        return NMS;
    }

    public static WorldGuardHook getWG() {
        return WG;
    }

    public static PartyHook getParties() {
        return PARTIES;
    }

}