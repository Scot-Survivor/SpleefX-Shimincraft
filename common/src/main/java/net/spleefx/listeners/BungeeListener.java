///*
// * This file is part of SpleefX, licensed under the MIT License.
// *
// *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
// *
// *  Permission is hereby granted, free of charge, to any person obtaining a copy
// *  of this software and associated documentation files (the "Software"), to deal
// *  in the Software without restriction, including without limitation the rights
// *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// *  copies of the Software, and to permit persons to whom the Software is
// *  furnished to do so, subject to the following conditions:
// *
// *  The above copyright notice and this permission notice shall be included in all
// *  copies or substantial portions of the Software.
// *
// *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// *  SOFTWARE.
// */
//package net.spleefx.listeners;
//
//import com.google.common.io.ByteArrayDataOutput;
//import com.google.common.io.ByteStreams;
//import net.spleefx.SpleefX;
//import net.spleefx.annotation.RegisteredListener;
//import net.spleefx.arena.Arenas;
//import net.spleefx.arena.MatchArena;
//import net.spleefx.arena.engine.ReloadedArenaEngine.JoinResult;
//import net.spleefx.arena.player.MatchPlayer;
//import net.spleefx.event.SpleefXEvent;
//import net.spleefx.event.arena.end.PostArenaEndEvent;
//import net.spleefx.event.listen.EventListener;
//import net.spleefx.event.player.PlayerQuitArenaEvent;
//import net.spleefx.util.message.message.Message;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.PlayerJoinEvent;
//import org.bukkit.event.player.PlayerLoginEvent;
//import org.bukkit.event.player.PlayerLoginEvent.Result;
//import org.jetbrains.annotations.NotNull;
//
//import static net.spleefx.config.SpleefXConfig.*;
//import static net.spleefx.util.game.Chat.colorize;
//
//@RegisteredListener
//public class BungeeListener implements Listener, EventListener {
//
//    public BungeeListener() {
//        Bukkit.getMessenger().registerOutgoingPluginChannel(SpleefX.getPlugin(), "BungeeCord");
//    }
//
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
//    public void onPlayerLogin(PlayerLoginEvent event) {
//        if (!BUNGEE_ENABLED.get()) return;
//        MatchArena arena = getArena();
//        boolean canDisallow = !event.getPlayer().isOp();
//        if (arena == null) {
//            if (canDisallow)
//                event.disallow(Result.KICK_OTHER, colorize(Message.NO_AVAILABLE_ARENA.getValue()));
//            return;
//        }
//        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
//        JoinResult result = arena.getEngine().playerJoin(player, false, false);
//        if (!result.isAllowed()) {
//            if (canDisallow)
//                event.disallow(Result.KICK_OTHER, colorize(result.getMessage()));
//        }
//    }
//
//    @EventHandler(ignoreCancelled = true) public void onPlayerJoin(PlayerJoinEvent event) {
//        MatchArena arena = getArena();
//        boolean canDisallow = !event.getPlayer().isOp();
//        if (arena == null) {
//            if (canDisallow)
//                event.getPlayer().kickPlayer(colorize(Message.NO_AVAILABLE_ARENA.getValue()));
//            return;
//        }
//        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
//        JoinResult result = arena.getEngine().playerJoin(player, false, true);
//        if (!result.isAllowed()) {
//            if (canDisallow)
//                event.getPlayer().kickPlayer(colorize(Message.NO_AVAILABLE_ARENA.getValue()));
//        }
//    }
//
//    private MatchArena getArena() {
//        if (BUNGEE_ARENA.get().isEmpty()) {
//            return Arenas.pick();
//        }
//        MatchArena arena = MatchArena.getByKey(BUNGEE_ARENA.get());
//        if (arena == null)
//            throw new IllegalArgumentException("Cannot find an arena by key " + BUNGEE_ARENA.get() + "!");
//        return arena;
//    }
//
//    @Override public void onEvent(@NotNull SpleefXEvent e) {
//        if (e instanceof PostArenaEndEvent) {
//            for (MatchPlayer player : ((PostArenaEndEvent) e).getTrackedPlayers()) {
//                sendToServer(player.player());
//            }
//        }
//        if (e instanceof PlayerQuitArenaEvent) {
//            sendToServer(((PlayerQuitArenaEvent) e).getPlayer());
//        }
//    }
//
//    private void sendToServer(Player player) {
//        ByteArrayDataOutput out = ByteStreams.newDataOutput();
//        out.writeUTF("Connect");
//        out.writeUTF(BUNGEE_KICK_TO_SERVER.get());
//        player.sendPluginMessage(SpleefX.getPlugin(), "BungeeCord", out.toByteArray());
//    }
//}
