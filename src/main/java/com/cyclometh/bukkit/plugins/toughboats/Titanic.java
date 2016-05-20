package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/** Core class of the Titanic plugin. Handles config, registration and reload command */
public class Titanic extends JavaPlugin
{
    static Logger LOGGER;

    private BoatMoveListener   moveListener;
    private BoatPacketListener packetListener;

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
            LOGGER.warning("It appears no Titanic features are enabled.");
            LOGGER.warning("Please check the config at `plugins/Titanic/config.yml`.");
            return;
        }

        if (Config.unsinkableUnoccupiedBoats)
            moveListener = new BoatMoveListener(this);

        if (Config.unsinkableOccupiedBoats)
            packetListener = new BoatPacketListener(this);

        LOGGER.fine("Plugin fully enabled");
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);

        moveListener   = null;
        packetListener = null;

        LOGGER.fine("Plugin fully disabled; all listeners and tasks unregistered");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        onDisable();
        onEnable();

        sender.sendMessage("[Titanic] Reloaded plugin and config.yml");
        return true;
    }
}
