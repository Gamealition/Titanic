package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

/** Core class of the Titanic plugin. Handles listener creation and reload command */
public class Titanic extends JavaPlugin implements Listener
{
    static Logger LOGGER;

    @Override
    public void onLoad()
    {
        LOGGER = getLogger();
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        LOGGER.fine("Plugin fully enabled; listening for boat events");
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Listener) this);
        LOGGER.fine("Plugin fully disabled; listener unregistered");
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    /** This has to use deprecated .getData methods, as Bukkit has no API for getting water level */
    public void onBoatMove(VehicleMoveEvent event)
    {
        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        Entity   rider = event.getVehicle().getPassenger();
        Boat     boat  = (Boat) event.getVehicle();
        Location locTo = event.getTo();
        Vector   vel   = boat.getVelocity();
        Block    under = locTo.getBlock();                // New object created
        Block    block = locTo.add(0, 0.6, 0).getBlock(); // WARN: Mutates locTo; new object created

        // Handle empty boat. Non-player riders are considered "empty" as the server still handles
        // their velocity and position updates
        if (rider == null || rider.getType() != EntityType.PLAYER)
        {
            // Deal with boats that sink into blocks after running aground
            if ( under.getType().isSolid() )
                boat.teleport(locTo);
            // Deal with boats that are inside water
            else if ( block.isLiquid() )
                boat.setVelocity( vel.setY(0.1) );
            return;
        }

        // From this point, handle boat ridden by a player. This needs special treatment as client
        // handles movement, so server must fight against it.

        if ( !block.isLiquid() )
            return;

        Location locFrom   = event.getFrom();
        Block    above     = block.getRelative(BlockFace.UP);
        byte     blockData = block.getData();
        byte     aboveData = above.getData();

        // Using doubles instead of making a new vector; avoids generating garbage per tick
        double velX = locTo.getX() - locFrom.getX();
        double velY = locTo.getY() - locFrom.getY() - 0.6;
        double velZ = locTo.getZ() - locFrom.getZ();

        // Whilst ridden by a player, server's boat velocity is always 0. Thus, preparing own
        // recorded velocity into the vector for later possible use
        vel.setX(velX).setZ(velZ);

        /**
         * Boat and block position findings, from logging (and lots of trial & error):
         *
         * * If boat's block has data 0, then boat is underwater or is sinking
         *   * If above block is air, then boat is in shallow water
         *   * If above block is water with data 0, then boat is deep underwater
         *   * If above block is water with data 1 and 15, then boat is under a slope/waterfall
         * * If boat's block has data between 1 and 7, then boat is riding a slope
         *   * Can be assumed that above block is AIR
         * * If boat's block has data between 8 and 15, then boat is inside waterfall
         *   * If above block has data between 0 and 7, then that is waterfall terminus
         *   * If above block has data between 8 and 15, then waterfall is continuing higher
         */

        // Deal with underwater/sinking boat
        if (blockData == 0)
        {
            // Deal with boats inside slope
            if (aboveData >= 1)
            {
                // Compensate based on how thick the slope is, with maximum
                velY = ( (double) (8 - blockData) ) / 15;
                velY = Math.min(velY, 0.4);
            }
            // Deal with boats that are just sinking vertically
            else
                // Compensate based on currently recorded sinking velocity
                velY = Math.abs(velY) + 0.1;
        }

        // Deal with boats riding top-most slope. Too jumpy if all slopes handled
        else if (blockData == 1)
            velY = 0.175;

        // Discontinue if boat is in intermediate slopes
        else if (blockData > 1 && blockData < 8)
            return;

        // Deal with boats inside a waterfall
        else if (blockData >= 8)
            velY = (aboveData >= 8 ? 1 : 0.5);

        // Apply velocity adjustment; interrupts client-side momentum with unpredictable results
        boat.setVelocity( vel.setY(velY) );
    }
}
