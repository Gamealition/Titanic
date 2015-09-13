package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

/**
 * Core class of the ToughBoats plugin. Registers boat events and a scheduled task for
 * periodically synchronizing player boat positions.
 */
public class ToughBoats extends JavaPlugin
{
    static Logger LOGGER;

    private BukkitTask        moveTask;
    private BoatEventListener boatListener;
    private BoatMoveListener  moveListener;

    @Override
    public void onLoad()
    {
        LOGGER = getLogger();
    }

    @Override
    public void onEnable()
    {
        Config.init(this);

        if ( Config.isNothingEnabled() )
        {
            LOGGER.warning("It appears no ToughBoats features are enabled.");
            LOGGER.warning("Please check the config at `plugins/ToughBoats/config.yml`.");
            return;
        }

        // Boat damage protection feature
        if (Config.protectBoats)
        {
            this.boatListener = new BoatEventListener(this);
            getServer().getPluginManager().registerEvents(this.boatListener, this);
        }
        else
            LOGGER.fine("Boat protection disabled in config.yml");

        // Boat client-server resynchronization feature
        if (Config.resyncBoats)
        {
            this.moveListener = new BoatMoveListener(this);
            getServer().getPluginManager().registerEvents(this.moveListener, this);

            int purgeInterval = this.getConfig().getInt("purge-interval", 10) * 20;
            this.moveTask     = Bukkit.getScheduler().runTaskTimer(
                    this, moveListener, purgeInterval, purgeInterval);
        }
        else
            LOGGER.fine("Player location synchronization disabled in config.yml");

        LOGGER.fine("Enabled; listeners and tasks registered");
    }

    @Override
    public void onDisable()
    {
        if (moveTask != null)
            Bukkit.getScheduler().cancelTask( moveTask.getTaskId() );

        if (this.moveListener != null)
            this.moveListener.cancelTask();

        HandlerList.unregisterAll(this);
        this.moveTask      = null;
        this.boatListener  = null;
        this.moveListener  = null;

        LOGGER.fine("Disabled; all listeners unregistered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if ( args.length < 1 || !args[0].equalsIgnoreCase("reload") )
            return false;

        this.onDisable();
        this.onEnable();

        if (sender != getServer().getConsoleSender())
            sender.sendMessage("[ToughBoats] Reloaded plugin and config.yml");

        getLogger().info("Reloaded plugin and config.yml");
        return true;
    }
}
