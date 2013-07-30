package com.cyclometh.bukkit.plugins.toughboats;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BoatMoveListener extends BukkitRunnable implements Listener {

	private Map<Integer, Calendar> entityList;
	//We hold a reference to the plugin so we can
	//access the logger.
	ToughBoats plugin;
	int syncInterval;
	int entityTTL;
	int syncDelay;
	public BoatMoveListener(ToughBoats plugin)
	{
		this.plugin=plugin;
		this.entityList=new HashMap<Integer, Calendar>();
		this.syncInterval=plugin.getConfig().getInt("sync-interval", 60) * 1000;
		this.entityTTL=plugin.getConfig().getInt("entity-ttl", 120) * 1000;
		this.syncDelay=plugin.getConfig().getInt("sync-delay", 2);
	}
	
	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event){
		
		int entityId;
		Calendar now;
		Vehicle boat;
		Player rider;
		
		//if it's not a boat, we're not interested.
		if (event.getVehicle().getType()!=EntityType.BOAT)
			return;
		//if there's no passenger, we don't care.
		if(event.getVehicle().getPassenger() == null)
			return;
		//if the boat doesn't have a player in it, we don't care either.
		if(event.getVehicle().getPassenger().getType()!=EntityType.PLAYER)
			return;
		
		//get the entity ID to track it.
		entityId=event.getVehicle().getEntityId();
		//track the entity. If we don't track it right now, we'll log it
		//and move on.
		if(!this.entityList.containsKey(entityId))
		{
			plugin.getLogger().info(String.format("Adding entity ID %d to list.", entityId));
			entityList.put(entityId, Calendar.getInstance()); //store the time we saw it last.
			return;
		}
		
		//if we get here, the entity was already tracked. So let's see how long it's been.
		now=Calendar.getInstance();
		Calendar then=this.entityList.get(entityId);
		long diff=now.getTimeInMillis()-then.getTimeInMillis();
		
		//if it's been more than a minute...
		if(diff > this.syncInterval)
			{
				boat=event.getVehicle();
				rider=(Player) boat.getPassenger();
				if(plugin.getConfig().getBoolean("debug", false))
				{
					plugin.getLogger().info(String.format("Creating teleport task for entity ID %d. Location: X%d Y%d Z%d.", 
							entityId, 
							(int)rider.getLocation().getX(), 
							(int)rider.getLocation().getY(), 
							(int)rider.getLocation().getZ()));
				}
				//schedule the task to run.
				Bukkit.getScheduler().runTaskLater(this.plugin, new TeleportTask(rider.getLocation(), boat, this.plugin), this.syncDelay);
				//stop tracking it. Next time a move event is registered it'll be retracked.
				entityList.remove(entityId);
			}
				
	}

	@Override
	public void run() {
		//purge entities that were tracked but stoppped moving before being resynchronized.
		Calendar now;
		now=Calendar.getInstance();
		Iterator<Map.Entry<Integer, Calendar>> entries = entityList.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry<Integer, Calendar> entry = entries.next();
		    if(now.getTimeInMillis()-entry.getValue().getTimeInMillis() > this.entityTTL)
		    {
		    	entries.remove();
		    }
		    
		}
	}
}

