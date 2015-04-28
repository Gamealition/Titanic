/*
 * Class: BoatEventsListener
 * Listens for a VehicleDestroyedEvent, checks if the vehicle is a Boat entity, and
 * if necessary, will cancel the event.
 */

package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.metadata.FixedMetadataValue;

/*
 * Listens for and cancels any boat destruction events.
 */
public class BoatEventListener implements Listener {

	//We hold a reference to the plugin so we can
	//access the logger.
	ToughBoats plugin;
	public BoatEventListener(ToughBoats plugin)
	{
		this.plugin=plugin;
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
            event.getVehicle().setMetadata( "hitCactus", new FixedMetadataValue(plugin, true) );
    }

	/*
	 * Listen for VehicleDestroyedEvent.
	 */
	@EventHandler
	public void onVehicleDestroyed(VehicleDestroyEvent event){

		//if it's not a boat, we're not interested.
		if (event.getVehicle().getType()!=EntityType.BOAT)
			return;

		//if it's already cancelled, we don't care.
		if(event.isCancelled())
			return;

		//if damage is from fire or lava, we don't try to stop it.
		//getFireTicks() will be -1 if the boat isn't on fire.
		if(event.getVehicle().getFireTicks() != -1)
			return;

        // If damage is from cactus, don't stop it. This allows boat collection systems
        // to work as intended. Relies on metadata set in onVehicleCollide
        if ( event.getVehicle().hasMetadata("hitCactus") )
            return;

        if (event.getAttacker() == null)
		{
			event.setCancelled(true);
			if(plugin.getConfig().getBoolean("debug", false))
				plugin.getLogger().info(String.format("Boat destruction prevented. Player: %s. Location: X%d Y%d Z%d.",
						((Player) event.getVehicle().getPassenger()).getName(),
						(int)event.getVehicle().getLocation().getX(),
						(int)event.getVehicle().getLocation().getY(),
						(int)event.getVehicle().getLocation().getZ()
						));

		}
		
		
	}
}
