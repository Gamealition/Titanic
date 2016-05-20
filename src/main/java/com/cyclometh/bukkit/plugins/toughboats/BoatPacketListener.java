package com.cyclometh.bukkit.plugins.toughboats;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

/**
 * Monitors the movement of boats being driven by players and attempts to prevent their
 * sinking by higher currents. See https://bugs.mojang.com/browse/MC-91206
 */
public class BoatPacketListener extends PacketAdapter implements Listener
{
    private static ToughBoats PLUGIN;
    private static Logger     LOGGER;

    public BoatPacketListener(ToughBoats plugin)
    {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.VEHICLE_MOVE);

        PLUGIN = plugin;
        LOGGER = ToughBoats.LOGGER;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Unsinkable occupied boats enabled; listening for boat move events");
    }

    @EventHandler
    public void onBoatLeave(VehicleExitEvent event)
    {
        // Only track boats with player riders
        if (event.getVehicle().getType() != EntityType.BOAT)
            return;
        else if (event.getExited().getType() != EntityType.PLAYER)
            return;

        final Boat boat = (Boat) event.getVehicle();

        // Force a position update after dismounting, as sometimes boat appears at older position.
        // Requires a task scheduled a tick later, as teleporting within event has no effect.
        Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, () ->
        {
            Location loc = boat.getLocation();
            boat.teleport(loc.clone().add(0, 5, 5));
            boat.teleport( loc );
        });
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onVehicleMove(VehicleMoveEvent event)
    {
        Boat   boat;
        Vector vel;
        double velX, velY, velZ;

        if (event.getVehicle().getType() != EntityType.BOAT)
            return;
        else if (event.getVehicle().getPassenger() == null)
            return;
        else if (event.getVehicle().getPassenger().getType() != EntityType.PLAYER)
            return;

        boat = (Boat) event.getVehicle();
        vel  = boat.getVelocity();
        velX = event.getTo().getX() - event.getFrom().getX();
        velY = event.getTo().getY() - event.getFrom().getY();
        velZ = event.getTo().getZ() - event.getFrom().getZ();

        // Compensate for when boat is underwater
        if ( velY < -0.01 )
        if ( boat.getLocation().add(0, 0.6, 0).getBlock().isLiquid() )
        {
            byte data = boat.getLocation().add(0, 0.5, 0).getBlock().getData();

            if (data > 4 && data < 8)
                return;

            // Base compensation off current velocity plus a bit more; fights against client
            velY = Math.abs(velY) + 0.1;
            // Impose maximum velocity; higher numbers may cause boat to fly large distances
            velY = Math.min(velY, 0.6);
//            boat.setVelocity( vel.setX(velX).setY(velY).setZ(velZ) );
        }


    }

}

