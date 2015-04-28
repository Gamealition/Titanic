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

/**
 * Monitors the movement of boats being driven by players, to schedule periodic resyncs
 * of their positions
 */
public class BoatMoveListener implements Runnable, Listener
{
    private ToughBoats plugin;
    private BukkitTask teleportTask;

    private int syncInterval;
    private int entityTTL;
    private int syncDelay;

    private Map<Integer, Calendar> entityList;

    public BoatMoveListener(ToughBoats plugin)
    {
        this.plugin       = plugin;
        this.entityList   = new HashMap<>();
        this.syncInterval = plugin.getConfig().getInt("sync-interval", 60) * 1000;
        this.entityTTL    = plugin.getConfig().getInt("entity-ttl", 120) * 1000;
        this.syncDelay    = plugin.getConfig().getInt("sync-delay", 2);
    }

    public void cancelTask()
    {
        if (teleportTask != null)
            Bukkit.getScheduler().cancelTask( teleportTask.getTaskId() );
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
            if (plugin.debugging)
                plugin.getLogger().info( String.format("Adding entity ID %d to list.", entityId) );

            entityList.put(entityId, Calendar.getInstance());
            return;
        }

        // If we get here, we were tracking the boat
        Calendar now  = Calendar.getInstance();
        Calendar then = this.entityList.get(entityId);
        long diff     = now.getTimeInMillis() - then.getTimeInMillis();

        if (diff < this.syncInterval)
            return;

        boat  = event.getVehicle();
        rider = (Player) boat.getPassenger();
        if (plugin.debugging)
            plugin.getLogger().info(String.format("Creating teleport task for entity ID %d. Location: X%d Y%d Z%d.",
                    entityId,
                    (int) rider.getLocation().getX(),
                    (int) rider.getLocation().getY(),
                    (int) rider.getLocation().getZ()));

        teleportTask = Bukkit.getScheduler().runTaskLater(
                plugin, new TeleportTask(rider.getLocation(), boat, plugin), syncDelay);

        // Stop tracking. Next time a move event is registered it'll be retracked.
        entityList.remove(entityId);
    }

    @Override
    public void run()
    {
        // Purge entities that were tracked but stopped moving before being resynchronized.
        if (plugin.debugging)
            plugin.getLogger().info(String.format("Purging entity list. %d items in list before purge.", entityList.size()));

        Calendar now = Calendar.getInstance();

        Iterator<Map.Entry<Integer, Calendar>> entries = entityList.entrySet().iterator();

        while ( entries.hasNext() )
        {
            Map.Entry<Integer, Calendar> entry = entries.next();

            if (now.getTimeInMillis() - entry.getValue().getTimeInMillis() > this.entityTTL)
                entries.remove();
        }

        if (plugin.debugging)
            plugin.getLogger().info(String.format("Entity list purge complete. %d items in list after purge.", entityList.size()));
    }
}

