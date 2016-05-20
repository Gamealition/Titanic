package com.cyclometh.bukkit.plugins.toughboats;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    @SuppressWarnings("deprecation")
    /** This has to use deprecated .getData methods, as Bukkit has no API for getting water level */
    public void onBoatMove(VehicleMoveEvent event)
    {
        // Only handle boats with a player rider
        if (event.getVehicle().getType() != EntityType.BOAT)
            return;
        else if (event.getVehicle().getPassenger() == null)
            return;
        else if (event.getVehicle().getPassenger().getType() != EntityType.PLAYER)
            return;

        Boat     boat    = (Boat) event.getVehicle();
        Location locFrom = event.getFrom();
        Location locTo   = event.getTo();
        Vector   vel     = boat.getVelocity();
        double   velX    = locTo.getX() - locFrom.getX();   // Using doubles instead of making a
        double   velY    = locTo.getY() - locFrom.getY();   // new vector using .subtract, to avoid
        double   velZ    = locTo.getZ() - locFrom.getZ();   // making garbage every tick.
        Block    block   = locTo.add(0, 0.6, 0).getBlock(); // WARN: Mutates locTo

        // Only handle underwater boats
        if ( !block.isLiquid() )
            return;

        Block    above    = block.getRelative(BlockFace.UP);
        Material blockMat = block.getType();
        Material aboveMat = above.getType();
        byte     blockData = block.getData();
        byte     aboveData = above.getData();

        /**
         * Boat and block position findings, from logging:
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

//        LOGGER.fine(String.format("Inside: %s:%s Above: %s:%s",
//            blockMat, blockData, aboveMat, aboveData
//        ));

        // Deal with underwater/sinking boat
        if (blockData == 0)
        {
            // Deal with boats inside slope
            if (aboveData >= 1)
            {
                // Compensate based on how thick the slope is
                velY = ( (double) (8 - blockData) ) / 15;
                velY = Math.min(velY, 0.4);
                boat.setVelocity( vel.setX(velX).setY(velY).setZ(velZ) );

                LOGGER.fine(String.format("Fixing under-slope boat: X%.3f Y%.3f Z%.3f",
                    velX, velY, velZ
                ));
            }
            // Deal with boats that are just sinking vertically
            else
            {
                // Compensate based on currently recorded sinking velocity
                velY = Math.abs(velY) + 0.1;
                boat.setVelocity( vel.setX(velX).setY(velY).setZ(velZ) );

                LOGGER.fine(String.format("Fixing underwater/sinking boat: X%.3f Y%.3f Z%.3f",
                    velX, velY, velZ
                ));
            }

            return;
        }

        // Deal with boats riding a slope
        // This makes slopes very jumpy, so it only affects upper ramps
        if (blockData == 1)
        {
            velY = ( (double) (8 - blockData) ) / 40;
            boat.setVelocity( vel.setX(velX).setY(velY).setZ(velZ) );

            LOGGER.fine(String.format("Fixing in-slope boat: X%.3f Y%.3f Z%.3f",
                velX, velY, velZ
            ));

            return;
        }

        // Deal with boats inside a waterfall
        if (blockData >= 8)
        {
            velY = (aboveData >= 8 ? 1 : 0.5);
            boat.setVelocity( vel.setX(velX).setY(velY).setZ(velZ) );

            LOGGER.fine(String.format("Fixing waterfall boat: X%.3f Y%.3f Z%.3f",
                velX, velY, velZ
            ));
        }

    }

}

