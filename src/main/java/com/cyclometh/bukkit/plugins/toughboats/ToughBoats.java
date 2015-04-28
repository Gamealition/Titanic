/*
 * ToughBoats- a Bukkit plugin.
 * Class: ToughBoats
 * Description: ToughBoats makes it impossible to break a boat object in Minecraft unless
 * you intentionally break it by damaging it with a tool or hand. If the boat takes damage 
 * from the environment by running into something, the plugin will prevent the boat from being
 * destroyed.
 * Author: Cyclometh (cyclometh@gmail.com)
 * Version: 1.0
 * 
 */
package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public class ToughBoats extends JavaPlugin
{
    private BukkitTask        task;
    private BoatEventListener eventListener;
    private BoatMoveListener  moveListener;

    private int purgeInterval;

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();

        if ( this.getConfig().getBoolean("protectboats", true) )
        {
            getLogger().info("Boat protection enabled!");
            this.eventListener = new BoatEventListener(this);
            getServer().getPluginManager().registerEvents(this.eventListener, this);
        }
        else
            getLogger().info("Boat protection disabled in config.yml.");


        if (this.getConfig().getBoolean("resync", false))
        {
            getLogger().info("Player location synchronization enabled!");
            this.moveListener = new BoatMoveListener(this);
            getServer().getPluginManager().registerEvents(this.moveListener, this);
            this.purgeInterval = this.getConfig().getInt("purge-interval", 10) * 20;
            this.task          = Bukkit.getScheduler().runTaskTimer(this, moveListener, this.purgeInterval, this.purgeInterval);
        }
        else
            getLogger().info("Player location synchronization disabled in config.yml.");
    }

    @Override
    public void onDisable()
    {
        if (task != null)
            Bukkit.getScheduler().cancelTask(task.getTaskId());

        this.task          = null;
        this.eventListener = null;
        this.moveListener  = null;
        getLogger().info("Disabled; no longer protecting boats");
    }


}
