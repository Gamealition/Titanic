package com.cyclometh.bukkit.plugins.toughboats;


import org.bukkit.configuration.Configuration;

/**
 * Static container class for plugin's configuration values
 */
class Config
{
    static Configuration config;

    /** Whether unoccupied boat positions are periodically resync'd */
    static boolean unsinkableUnoccupiedBoats = true;
    /** Whether occupied boat positions are periodically resync'd */
    static boolean unsinkableOccupiedBoats   = true;

    static void init(Titanic plugin)
    {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        config = plugin.getConfig();

        unsinkableUnoccupiedBoats = config.getBoolean("unsinkableboats.unoccupied", unsinkableUnoccupiedBoats);
        unsinkableOccupiedBoats   = config.getBoolean("unsinkableboats.occupied", unsinkableOccupiedBoats);
    }

    static boolean isNothingEnabled()
    {
        return !unsinkableOccupiedBoats && !unsinkableUnoccupiedBoats;
    }

    private Config() { }
}