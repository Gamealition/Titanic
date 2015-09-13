package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Core class of the ToughBoats plugin. Registers boat events and a scheduled task for
 * periodically synchronizing player boat positions.
 */
public class ToughBoats extends JavaPlugin
{
    static Logger LOGGER;

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

        if (Config.protectBoats)
            boatListener = new BoatEventListener(this);

        if (Config.resyncBoats)
            moveListener = new BoatMoveListener(this);

        LOGGER.fine("Plugin fully enabled");
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        boatListener = null;
        moveListener = null;

        LOGGER.fine("Plugin fully disabled; all listeners and tasks unregistered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if ( args.length < 1 || !args[0].equalsIgnoreCase("reload") )
            return false;

        onDisable();
        onEnable();

        sender.sendMessage("[ToughBoats] Reloaded plugin and config.yml");
        return true;
    }
}
