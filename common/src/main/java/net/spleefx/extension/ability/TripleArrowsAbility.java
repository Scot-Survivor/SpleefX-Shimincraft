/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.extension.ability;

import com.cryptomorin.xseries.XEnchantment;
import net.spleefx.annotation.RegisteredListener;
import net.spleefx.arena.bow.BowSpleefListener;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.type.bowspleef.BowSpleefArena;
import net.spleefx.arena.type.bowspleef.extension.TripleArrowsOptions;
import net.spleefx.backend.DelayContext;
import net.spleefx.backend.Schedulers;
import net.spleefx.extension.StandardExtensions;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
@RegisteredListener
public class TripleArrowsAbility implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (StandardExtensions.BOW_SPLEEF.getTripleArrows().getActionsToTrigger() == null || !StandardExtensions.BOW_SPLEEF.getTripleArrows().getActionsToTrigger().contains(event.getAction()))
            return;
        MatchPlayer player = MatchPlayer.wrap(event.getPlayer());
        if (player.getArena() == null || !(player.getArena() instanceof BowSpleefArena)) return;
        BowSpleefArena arena = player.getArena();
        TripleArrowsOptions settings = StandardExtensions.BOW_SPLEEF.getTripleArrows();
        if (settings.getRequiredMaterials().contains(player.getMainHand().getType()))
            launchTripleArrowsIfPossible(player, arena);
    }

    private void launchTripleArrowsIfPossible(MatchPlayer player, BowSpleefArena arena) {
        if (!StandardExtensions.BOW_SPLEEF.getTripleArrows().isEnabled()) return;
        if (Schedulers.DELAY.hasDelay(player, DelayContext.TRIPLE_ARROWS)) return;
        Player p = player.player();
        if (arena.getEngine().getAbilities().get(p, GameAbility.TRIPLE_ARROWS) <= 0)
            return; // Player has no more double jumps
        ItemStack mainHand = player.getMainHand();
        boolean flame = mainHand.getType().name().contains("BOW") &&
                mainHand.getItemMeta().hasEnchant(XEnchantment.ARROW_FIRE.getEnchant());
        Vector playerVector = player.getLocation().getDirection();

        Arrow first = p.launchProjectile(Arrow.class, playerVector);
        BowSpleefListener.ARROW.set(first, arena);

        Arrow second = p.launchProjectile(Arrow.class, rotateAroundY(playerVector, Math.toRadians(-45)));
        BowSpleefListener.ARROW.set(second, arena);

        Arrow third = p.launchProjectile(Arrow.class, rotateAroundY(playerVector, Math.toRadians(90)));
        BowSpleefListener.ARROW.set(third, arena);

        if (flame) {
            first.setFireTicks(Integer.MAX_VALUE);
            second.setFireTicks(Integer.MAX_VALUE);
            third.setFireTicks(Integer.MAX_VALUE);
        }
        Schedulers.DELAY.delay(player, DelayContext.TRIPLE_ARROWS, StandardExtensions.BOW_SPLEEF.getTripleArrows().getCooldown(), TimeUnit.SECONDS);
        arena.getEngine().getAbilities().consume(p, GameAbility.TRIPLE_ARROWS);
    }

    @NotNull
    private static Vector rotateAroundY(Vector v, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * v.getX() + angleSin * v.getZ();
        double z = -angleSin * v.getX() + angleCos * v.getZ();
        return v.setX(x).setZ(z);
    }
}
