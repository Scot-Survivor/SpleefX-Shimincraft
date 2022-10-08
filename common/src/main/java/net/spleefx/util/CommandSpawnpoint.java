////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package net.spleefx.util;
//
//import net.minecraft.commands.CommandDispatcher;
//import net.minecraft.commands.CommandListenerWrapper;
//import net.minecraft.commands.arguments.ArgumentAngle;
//import net.minecraft.commands.arguments.ArgumentEntity;
//import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
//import net.minecraft.core.BlockPosition;
//import net.minecraft.network.chat.ChatMessage;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.server.level.EntityPlayer;
//import net.minecraft.world.level.World;
//
//import java.util.Collection;
//import java.util.Collections;
//
//public class CommandSpawnpoint {
//
//    public CommandSpawnpoint() {
//    }
//
//    public static void a(com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> dispatcher) {
//        dispatcher.register(CommandDispatcher
//                .a("spawnpoint")
//                .requires((player) -> player.hasPermission(2))
//                .executes((context) ->
//                        a(context.getSource(), Collections.singleton(context.getSource().h()), new BlockPosition(context.getSource().getPosition()), 0.0F))
//                .then((CommandDispatcher.a("targets", ArgumentEntity.d())
//                        .executes((context) -> a(context.getSource(), ArgumentEntity.f(context, "targets"), new BlockPosition(context.getSource().getPosition()), 0.0F)))
//                        .then((CommandDispatcher.a("pos", ArgumentPosition.a())
//                                .executes((context) -> a(context.getSource(), ArgumentEntity.f(context, "targets"), ArgumentPosition.b(context, "pos"), 0.0F)))
//                                .then(CommandDispatcher.a("angle", ArgumentAngle.a()).executes((context) -> a(context.getSource(),
//                                        ArgumentEntity.f(context, "targets"),
//                                        ArgumentPosition.b(context, "pos"),
//                                        ArgumentAngle.a(context, "angle")))))));
//    }
//
//    private static int a(CommandListenerWrapper sender, Collection<EntityPlayer> players, BlockPosition pos, float angle) {
//        ResourceKey<World> world = sender.getWorld().getDimensionKey();
//
//        for (EntityPlayer player : players) {
//            player.setRespawnPosition(world, pos, angle, true, false);
//        }
//
//        String var5 = world.a().toString();
//        if (players.size() == 1) {
//            sender.sendMessage(new ChatMessage("commands.spawnpoint.success.single", pos.getX(), pos.getY(), pos.getZ(), angle, var5, players.iterator().next().getScoreboardDisplayName()), true);
//        } else {
//            sender.sendMessage(new ChatMessage("commands.spawnpoint.success.multiple", pos.getX(), pos.getY(), pos.getZ(), angle, var5, players.size()), true);
//        }
//
//        return players.size();
//    }
//}
