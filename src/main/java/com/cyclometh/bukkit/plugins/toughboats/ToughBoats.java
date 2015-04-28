package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Core class of the ToughBoats plugin. Registers boat events and a scheduled task for
 * periodically synchronizing player boat positions.
 */
public class ToughBoats extends JavaPlugin
{
    public boolean debugging;

    private BukkitTask        moveTask;
    private BoatEventListener eventListener;
    private BoatMoveListener  moveListener;

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        this.reloadConfig();
        this.debugging = this.getConfig().getBoolean("debug", false);

        if (debugging)
            getLogger().info("Debugging is enabled in config.yml");

        // Boat damage protection feature
        if ( this.getConfig().getBoolean("protectboats", true) )
        {
            this.eventListener = new BoatEventListener(this);
            getServer().getPluginManager().registerEvents(this.eventListener, this);
        }
        else if (debugging)
            getLogger().info("Boat protection disabled in config.yml");

        // Boat client-server resynchronization feature
        if ( this.getConfig().getBoolean("resync", true) )
        {
            this.moveListener = new BoatMoveListener(this);
            getServer().getPluginManager().registerEvents(this.moveListener, this);

            int purgeInterval = this.getConfig().getInt("purge-interval", 10) * 20;
            this.moveTask     = Bukkit.getScheduler().runTaskTimer(
                    this, moveListener, purgeInterval, purgeInterval);
        }
        else if (debugging)
            getLogger().info("Player location synchronization disabled in config.yml");

        if (eventListener == null && moveListener == null)
            getLogger().warning("No features are enabled; plugin is effectively useless");
        else if (debugging)
            getLogger().info("Enabled; listeners and tasks registered");
    }

    @Override
    public void onDisable()
    {
        if (moveTask != null)
            Bukkit.getScheduler().cancelTask( moveTask.getTaskId() );

        HandlerList.unregisterAll(this);
        this.moveListener.cancelTask();
        this.moveTask      = null;
        this.eventListener = null;
        this.moveListener  = null;

        if (debugging)
            getLogger().info("Disabled; all listeners unregistered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if ( args.length < 1 || !args[0].equalsIgnoreCase("reload") )
            return false;

        this.onDisable();
        this.onEnable();

        if (getServer().getConsoleSender() != sender)
            sender.sendMessage("[ToughBoats] Reloaded plugin and config.yml");

        getLogger().info("Reloaded plugin and config.yml");
        return true;
    }
}
