package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.logging.Logger;

/**
 * Listens for events relating to the damage or destruction of boats, cancelling them if
 * they are triggered by frivolous means
 */
public class BoatEventListener implements Listener
{
    private static final String CACTUS_HIT_TAG = "hitCactus";

    private static Logger        LOGGER;
    private static MetadataValue CACTUS_HIT;

    public BoatEventListener(ToughBoats plugin)
    {
        LOGGER = ToughBoats.LOGGER;

        if (CACTUS_HIT == null)
            CACTUS_HIT = new FixedMetadataValue(plugin, true);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Boat protection enabled; listening for boat damage events");
    }

    /**
     * Checks for boats that collide with cacti. This is needed as CraftBukkit does not
     * appear to pass cactus damage source/events to VehicleDestroyEvent.
     */
    @EventHandler
    public void onVehicleCollide(VehicleBlockCollisionEvent event)
    {
        if (event.getVehicle().getType() == EntityType.BOAT)
        if (event.getBlock().getType()   == Material.CACTUS)
        {
            event.getVehicle().setMetadata(CACTUS_HIT_TAG, CACTUS_HIT);

            LOGGER.fine("Tagged boat that collided with cactus");
        }
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event)
    {
        if ( event.getVehicle().getType() != EntityType.BOAT || event.isCancelled() )
            return;

        // Do not cancel fire damage
        if (event.getVehicle().getFireTicks() != -1)
            return;

        // If damage is from cactus, don't stop it. This allows boat collection systems
        // to work as intended. Relies on metadata set in onVehicleCollide
        if ( event.getVehicle().hasMetadata(CACTUS_HIT_TAG) )
            return;

        // If attacked by a player or entity, don't stop it
        if (event.getAttacker() != null)
            return;

        event.setCancelled(true);
        LOGGER.finer(String.format("Boat destruction prevented. Player: %s. Location: X%d Y%d Z%d.",
            event.getVehicle().getPassenger().getName(),
            (int) event.getVehicle().getLocation().getX(),
            (int) event.getVehicle().getLocation().getY(),
            (int) event.getVehicle().getLocation().getZ()
        ));
    }
}