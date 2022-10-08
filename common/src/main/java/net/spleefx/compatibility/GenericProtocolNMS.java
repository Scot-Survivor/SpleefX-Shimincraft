package net.spleefx.compatibility;

import lombok.SneakyThrows;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.UUID;

public class GenericProtocolNMS implements ProtocolNMS {


    @SneakyThrows
    @Override public Entity getEntity(UUID uuid) {
        try {
            return ProtocolNMS.super.getEntity(uuid);
        } catch (Throwable throwable) {
            try {
                Object dedicatedServer = getServer.invoke(Bukkit.getServer());
                Object entity = getByUUID.invoke(dedicatedServer, uuid);
                return (Entity) getBukkitEntity.invoke(entity);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    @Override public void setUnbreakable(ItemMeta item) {
        try {
            ProtocolNMS.super.setUnbreakable(item);
        } catch (Throwable ignored) {
            try {
                Object spigot = metaSpigot.invoke(item);
                setUnbreakable.invoke(spigot, true);
            } catch (Throwable ignored2) {
            }
        }
    }
    private static Method metaSpigot, getServer, getByUUID, getBukkitEntity, setUnbreakable;

    static {
        try {
            getServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer");
            getByUUID = getServer.invoke(Bukkit.getServer()).getClass().getMethod("a", UUID.class);
            getBukkitEntity = Protocol.getProtocolClass("Entity").getDeclaredMethod("getBukkitEntity");
            metaSpigot = ItemMeta.class.getDeclaredMethod("spigot");
            setUnbreakable = metaSpigot.getReturnType().getDeclaredMethod("setUnbreakable", boolean.class);
        } catch (Throwable ignored) {
        }
    }
}
