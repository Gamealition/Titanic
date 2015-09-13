package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Monitors the movement of boats being driven by players, to schedule a periodic resync
 * of their positions
 */
public class BoatMoveListener implements Runnable, Listener
{
    private final int ENTITY_TTL = 120 * 1000;
    private final int SYNC_DELAY = 2;

    private static ToughBoats PLUGIN;
    private static Logger     LOGGER;

    private Map<Integer, Calendar> entityList = new HashMap<>();

    private BukkitTask teleportTask;

    public BoatMoveListener(ToughBoats plugin)
    {
        PLUGIN = plugin;
        LOGGER = ToughBoats.LOGGER;
    }

    public void cancelTask()
    {
        if (teleportTask != null)
            Bukkit.getScheduler().cancelTask(teleportTask.getTaskId());
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event)
    {
        int     entityId;
        Vehicle boat;
        Player  rider;

        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        if (event.getVehicle().getPassenger() == null)
            return;

        if (event.getVehicle().getPassenger().getType() != EntityType.PLAYER)
            return;

        entityId = event.getVehicle().getEntityId();
        if ( !this.entityList.containsKey(entityId) )
        {
            LOGGER.finer(String.format("Adding entity ID %d to list.", entityId));

            entityList.put( entityId, Calendar.getInstance() );
            return;
        }

        // If we get here, we were tracking the boat
        Calendar now  = Calendar.getInstance();
        Calendar then = this.entityList.get(entityId);
        long diff     = now.getTimeInMillis() - then.getTimeInMillis();

        if (diff < Config.resyncInterval)
            return;

        boat  = event.getVehicle();
        rider = (Player) boat.getPassenger();

        LOGGER.finer(String.format("Creating teleport task for entity ID %d. Location: X%d Y%d Z%d.",
            entityId,
            (int) rider.getLocation().getX(),
            (int) rider.getLocation().getY(),
            (int) rider.getLocation().getZ()
        ));

        teleportTask = Bukkit.getScheduler().runTaskLater(
            PLUGIN, new TeleportTask(rider.getLocation(), boat, PLUGIN), SYNC_DELAY
        );

        // Stop tracking. Next time a move event is registered it'll be retracked.
        entityList.remove(entityId);
    }

    @Override
    public void run()
    {
        // TODO: Is all of the tracking code necessary nowadays?
        // Purge entities that were tracked but stopped moving before being resynchronized.
        LOGGER.finer(String.format("Purging entity list. %d items in list before purge.",
            entityList.size()
        ));

        Calendar now = Calendar.getInstance();

        Iterator<Map.Entry<Integer, Calendar>> entries = entityList.entrySet().iterator();

        while ( entries.hasNext() )
        {
            Map.Entry<Integer, Calendar> entry = entries.next();

            if (now.getTimeInMillis() - entry.getValue().getTimeInMillis() > this.ENTITY_TTL)
                entries.remove();
        }

        LOGGER.info(String.format("Entity list purge complete. %d items in list after purge.", entityList.size()));
    }
}

