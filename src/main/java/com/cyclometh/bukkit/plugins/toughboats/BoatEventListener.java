package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 * Listens for events relating to the damage or destruction of boats, cancelling them if
 * they are triggered by frivolous means
 */
public class BoatEventListener implements Listener
{
    private static final String hitCactusTag = "hitCactus";

    private ToughBoats    plugin;
    private MetadataValue hitCactus;

    public BoatEventListener(ToughBoats plugin)
    {
        this.plugin    = plugin;
        this.hitCactus = new FixedMetadataValue(plugin, true);
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
            event.getVehicle().setMetadata(hitCactusTag, hitCactus);

            if (plugin.debugging)
                plugin.getLogger().info("Tagged boat that collided with cactus");
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
        if ( event.getVehicle().hasMetadata(hitCactusTag) )
            return;

        if (event.getAttacker() == null)
        {
            event.setCancelled(true);
            if (plugin.debugging)
                plugin.getLogger().info(String.format("Boat destruction prevented. Player: %s. Location: X%d Y%d Z%d.",
                        ((Player) event.getVehicle().getPassenger()).getName(),
                        (int) event.getVehicle().getLocation().getX(),
                        (int) event.getVehicle().getLocation().getY(),
                        (int) event.getVehicle().getLocation().getZ()
                ));
        }
    }
}
