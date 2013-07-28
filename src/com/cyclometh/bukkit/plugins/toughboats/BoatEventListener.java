/*
 * Class: BoatEventsListener
 * Listens for a VehicleDestroyedEvent, checks if the vehicle is a Boat entity, and
 * if necessary, will cancel the event.
 */

package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

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
		
		if (event.getAttacker() == null && !event.isCancelled())
		{
			event.setCancelled(true);
			if(plugin.getConfig().getBoolean("debug"))
				plugin.getLogger().info("Boat destruction prevented.");
		}
		
		
	}
}
