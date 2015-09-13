package com.cyclometh.bukkit.plugins.toughboats;


import org.bukkit.configuration.Configuration;

/**
 * Static container class for plugin's configuration values
 */
class Config
{
    static Configuration config;

    /** Whether boats are protected from damage or destruction */
    static boolean protectBoats   = true;
    /** Whether boat positions are periodically resync'd */
    static boolean resyncBoats    = true;
    /** How often (in seconds) to perform resync */
    static int     resyncInterval = 60;

    static void init(ToughBoats plugin)
    {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        config = plugin.getConfig();

        protectBoats   = config.getBoolean("protectboats", protectBoats);
        resyncBoats    = config.getBoolean("resync", resyncBoats);
        resyncInterval = config.getInt("sync-interval", resyncInterval);
    }

    static boolean isNothingEnabled()
    {
        return !protectBoats && !resyncBoats;
    }

    private Config() { }
}