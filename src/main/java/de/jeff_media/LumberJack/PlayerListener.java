package de.jeff_media.LumberJack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	
	LumberJack plugin;
	
	public PlayerListener(LumberJack plugin) {
		this.plugin=plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		plugin.registerPlayer(e.getPlayer());
		
		if (e.getPlayer().isOp()) {
			plugin.updateChecker.sendUpdateMessage(e.getPlayer());
		}
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.unregisterPlayer(e.getPlayer());
	}
	

}