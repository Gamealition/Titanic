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
    private static ToughBoats      PLUGIN;
    private static Logger          LOGGER;

    public BoatMoveListener(ToughBoats plugin)
    {
        PLUGIN = plugin;
        LOGGER = ToughBoats.LOGGER;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Unsinkable unoccupied boats enabled; listening for boat move events");
    }

    @EventHandler
    /** Fires when boat moves whilst empty (drifting boats) */
    public void onVehicleMove(VehicleMoveEvent event)
    {
        Boat    boat;
        Vector  vel;
        double  velX, velY, velZ;

        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        boat = (Boat) event.getVehicle();
        vel  = boat.getVelocity();
        velX = vel.getX();
        velY = vel.getY();
        velZ = vel.getZ();

        // Compensate for when boat runs aground and sinks into block
        if ( velY < -0.01 && boat.getLocation().getBlock().getType().isOccluding() )
        {
            boat.teleport( boat.getLocation().add(0, 0.5, 0) );
            return;
        }

        // Proceed only if movement is large enough
        if (velX <  0.01 && velZ <  0.01)
        if (velX > -0.01 && velZ > -0.01)
            return;

        // Compensate for when boat is "underwater"
        if ( boat.getLocation().add(0,  0.5, 0).getBlock().isLiquid() )
            boat.setVelocity( vel.setY(0.06) );
    }

}

