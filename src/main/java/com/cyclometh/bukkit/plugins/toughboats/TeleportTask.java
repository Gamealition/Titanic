package com.cyclometh.bukkit.plugins.toughboats;


import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/*
 * Class TeleportTask
 * Description: A task object to manage resynchronizing players with their actual position.
 * It is implemented as a Bukkit task object because we don't want to be removing a boat
 * entity or teleporting a player from within the movement event handler.
 */
public class TeleportTask extends BukkitRunnable {

	private Location loc;
	private Vehicle boat;
	private JavaPlugin plugin;
	
	public TeleportTask(Location teleportTo, Vehicle boat, JavaPlugin plugin){
		if(boat.getType() != EntityType.BOAT)
		{
			throw new IllegalStateException("Cannot create an instance of TeleportTask with a non-boat vehicle.");
		}
		if(boat.getPassenger() == null)
		{
			throw new IllegalStateException("Cannot create an instance of TeleportTask with an empty boat.");
		}
		if(boat.getPassenger().getType() != EntityType.PLAYER)
		{
			throw new IllegalStateException("Cannot create an instance of TeleportTask with a non-player entity.");
		}
		this.loc=teleportTo;
		this.boat=boat;
		this.plugin=plugin;
	}
	
	@Override
	public void run() {
		Player passenger;
		if((passenger=(Player)boat.getPassenger()) == null)	//player got out?
			return;
		if(plugin.getConfig().getBoolean("debug", false))
		{
			plugin.getLogger().info(String.format("Resynchronizing player %s.", passenger.getName()));
		}
		
		Vector vel = boat.getVelocity();
		//eject the passenger, destroy the boat, add a new one,
		//then put the passenger in the new one.
		passenger.leaveVehicle();
		boat.remove();
		//set the yaw to the current facing.
		this.loc.setYaw(passenger.getLocation().getYaw());
		boat=(Boat)passenger.getWorld().spawn(this.loc, Boat.class);
		passenger.teleport(this.loc, TeleportCause.PLUGIN);
		boat.setPassenger(passenger);
		boat.setVelocity(vel);
		
		//probably not necessary, but can't hurt.
		this.loc=null;
		this.boat=null;
		passenger=null;
		
		
	}

}
