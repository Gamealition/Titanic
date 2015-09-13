package com.cyclometh.bukkit.plugins.toughboats;


import org.bukkit.configuration.Configuration;

import java.util.logging.Logger;

/**
 * Static container class for plugin's configuration values
 */
class Config
{
    private static Logger LOGGER;

    static Configuration config;

    /** Whether boats are protected from damage or destruction */
    static boolean protectBoats   = true;
    /** Whether boat positions are periodically resync'd */
    static boolean resyncBoats    = true;
    /** How often (in seconds) to perform resync */
    static int     resyncInterval = 60;

    static void init(ToughBoats plugin)
    {
        LOGGER = ToughBoats.LOGGER;

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        config = plugin.getConfig();

        protectBoats   = config.getBoolean("protectboats", protectBoats);
        resyncBoats    = config.getBoolean("resync", resyncBoats);
        resyncInterval = config.getInt("sync-interval", resyncInterval);

        if (resyncInterval < 5)
        {
            LOGGER.warning("sync-interval is set too low! Must be 5 or higher.");
            LOGGER.warning("It has been reset to the default 60 seconds.");
            resyncInterval = 5;
        }
    }

    static boolean isNothingEnabled()
    {
        return !protectBoats && !resyncBoats;
    }

    private Config() { }
}