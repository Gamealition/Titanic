package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

/**
 * Monitors the movement of unoccupied boats being pushed and attempts to prevent their sinking by
 * higher currents. See https://bugs.mojang.com/browse/MC-91206
 */
public class BoatMoveListener implements Listener
{
    private static ToughBoats PLUGIN;
    private static Logger     LOGGER;

    public BoatMoveListener(ToughBoats plugin)
    {
        PLUGIN = plugin;
        LOGGER = ToughBoats.LOGGER;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Unsinkable unoccupied boats enabled; listening for boat move events");
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event)
    {
        Boat   boat;
        Vector vel;

        // Only track boats
        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        // Only track boats that are unoccupied or have non-player riders
        if (event.getVehicle().getPassenger() != null)
        if (event.getVehicle().getPassenger().getType() == EntityType.PLAYER)
            return;

        boat = (Boat) event.getVehicle();
        vel  = boat.getVelocity();

        // Compensate for when boat runs aground and sinks into block
        if ( boat.getLocation().getBlock().getType().isOccluding() )
            boat.teleport( boat.getLocation().add(0, 0.5, 0) );
        // Compensate for when boat is underwater
        else if ( boat.getLocation().add(0, 0.5, 0).getBlock().isLiquid() )
            boat.setVelocity( vel.setY(0.06) );
    }
}

