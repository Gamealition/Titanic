package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
    public void onBoatMove(VehicleMoveEvent event)
    {
        // Only handle boats
        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        // Only handle boats that are unoccupied or have non-player riders
        if (event.getVehicle().getPassenger() != null)
        if (event.getVehicle().getPassenger().getType() == EntityType.PLAYER)
            return;

        Boat     boat  = (Boat) event.getVehicle();
        Location locTo = event.getTo();
        Vector   vel   = boat.getVelocity();
        Block    block = locTo.getBlock();
        Block    above = locTo.add(0, 0.6, 0).getBlock();

        // Deal with boats that sink into blocks after running aground
        if ( block.getType().isSolid() )
            boat.teleport(locTo);
        // Deal with boats that are inside water
        else if ( above.isLiquid() )
            boat.setVelocity( vel.setY(0.1) );
    }
}

