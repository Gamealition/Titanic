package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Core class of the ToughBoats plugin. Registers boat events and a scheduled task for
 * periodically synchronizing player boat positions.
 */
public class ToughBoats extends JavaPlugin
{
    public boolean debugging;

    private BukkitTask        task;
    private BoatEventListener eventListener;
    private BoatMoveListener  moveListener;

    private int purgeInterval;

    public void onLoad()
    {
        this.eventListener = new BoatEventListener(this);
        this.moveListener  = new BoatMoveListener(this);

    }

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        debugging = this.getConfig().getBoolean("debug", false);

        if ( this.getConfig().getBoolean("protectboats", true) )
            getServer().getPluginManager().registerEvents(this.eventListener, this);
        else
            getLogger().info("Boat protection disabled in config.yml");


        if (this.getConfig().getBoolean("resync", false))
        {
            getLogger().info("Player location synchronization enabled!");

            getServer().getPluginManager().registerEvents(this.moveListener, this);
            this.purgeInterval = this.getConfig().getInt("purge-interval", 10) * 20;
            this.task          = Bukkit.getScheduler().runTaskTimer(this, moveListener, this.purgeInterval, this.purgeInterval);
        }
        else
            getLogger().info("Player location synchronization disabled in config.yml");
    }

    @Override
    public void onDisable()
    {
        if (task != null)
            Bukkit.getScheduler().cancelTask( task.getTaskId() );

        this.task          = null;
        this.eventListener = null;
        this.moveListener  = null;
        getLogger().info("Disabled; no longer protecting boats");
    }


}
