package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * A task object to manage resynchronizing players with their actual position. It is
 * implemented as a task because we don't want to be removing a boat entity or teleporting
 * a player from within the movement event handler.
 */
public class TeleportTask implements Runnable
{
    ToughBoats plugin;

    private Location loc;
    private Boat     boat;

    public TeleportTask(Location teleportTo, Vehicle vehicle, ToughBoats plugin)
    {
        if (vehicle.getType() != EntityType.BOAT)
            throw new IllegalStateException("Cannot create an instance of TeleportTask with a non-boat vehicle.");

        if (vehicle.getPassenger() == null)
            throw new IllegalStateException("Cannot create an instance of TeleportTask with an empty boat.");

        if (vehicle.getPassenger().getType() != EntityType.PLAYER)
            throw new IllegalStateException("Cannot create an instance of TeleportTask with a non-player entity.");

        this.plugin = plugin;
        this.loc    = teleportTo;
        this.boat   = (Boat) vehicle;
    }

    /**
     * Replaces a passenger's boat by destroying it, recreating it and seating them in
     */
    @Override
    public void run()
    {
        Player passenger = (Player) boat.getPassenger();

        if (passenger == null) // player got out?
            return;

        if (plugin.debugging)
            plugin.getLogger().info( String.format("Resynchronizing player %s.", passenger.getName()) );

        Vector vel = boat.getVelocity();
        float  yaw = passenger.getLocation().getYaw();

        passenger.leaveVehicle();
        boat.remove();
        this.loc.setYaw(yaw);
        boat = passenger.getWorld().spawn(this.loc, Boat.class);
        passenger.teleport(this.loc, TeleportCause.PLUGIN);
        boat.setPassenger(passenger);
        boat.setVelocity(vel);

        // probably not necessary, but can't hurt.
        this.loc  = null;
        this.boat = null;
    }

}
