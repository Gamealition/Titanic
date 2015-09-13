package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.logging.Logger;

/**
 * Listens for events relating to the damage of players, caused by fall damage from boat
 * sailing. See https://bugs.mojang.com/browse/MC-881
 */
public class PlayerHurtListener implements Listener
{
    private static Logger LOGGER;

    public PlayerHurtListener(ToughBoats plugin)
    {
        LOGGER = ToughBoats.LOGGER;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Player protection enabled; listening for fall damage events");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerHurt(EntityDamageEvent event)
    {
        if ( !(event.getEntity() instanceof Player) )
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        Player player  = (Player) event.getEntity();
        Entity vehicle = player.getVehicle();

        if ( vehicle == null || !(vehicle instanceof Boat) )
            return;

        event.setCancelled(true);
        LOGGER.finer(String.format("Fall damage %.2f negated whilst on boat. Player: %s.",
            event.getFinalDamage(),
            player.getName()
        ));
    }
}