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

import org.bukkit.plugin.java.JavaPlugin;

public class ToughBoats extends JavaPlugin {

	@Override
	public void onEnable(){
		getLogger().info("ToughBoats started! Now protecting any boats.");
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new BoatEventListener(this), this);
	}
	
	@Override
	public void onDisable(){
		getLogger().info("ToughBoats shut down. No longer protecting boats.");
	}
	
	
	
}
