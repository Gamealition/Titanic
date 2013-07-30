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

public class ToughBoats extends JavaPlugin {

	private BukkitTask task;
	private BoatEventListener eventListener;
	private BoatMoveListener moveListener;
	@Override
	public void onEnable(){
		getLogger().info("ToughBoats started! Now protecting any boats.");
		this.saveDefaultConfig();
		this.eventListener=new BoatEventListener(this);
		this.moveListener=new BoatMoveListener(this);
		getServer().getPluginManager().registerEvents(this.eventListener, this);
		getServer().getPluginManager().registerEvents(this.moveListener, this);
		
		//schedule the cleanup task. Run it every 10 seconds.
		task=Bukkit.getScheduler().runTaskTimer(this, moveListener, 200, 200);
	}
	
	@Override
	public void onDisable(){
		Bukkit.getScheduler().cancelTask(task.getTaskId());
		getLogger().info("ToughBoats shut down. No longer protecting boats.");
		
	}
	
	
	
}
