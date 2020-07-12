package de.jeff_media.LumberJack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
	
	private LumberJack plugin;
	
	BlockPlaceListener(LumberJack plugin) {
		this.plugin=plugin;
	}
	
	
	// Prevent torches and stuff being placed inside a falling log
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		
		if(!plugin.getConfig().getBoolean("prevent-torch-exploit")) {
			return;
		}
		
		if(e.getBlockPlaced().getType().isSolid()) {
			return;
		}
		
		//System.out.println("possible conflicting block has been placed");
		
		for(Entity entity : e.getBlock().getLocation().getWorld().getEntities()) {
			
			//System.out.println(entity.getType().name());
			
			if(!(entity instanceof FallingBlock)) {
				continue;
			}
			
			//System.out.println(("falling block detected while block was placed"));
			
			FallingBlock fallingBlock = (FallingBlock) entity;
			
			if(fallingBlock.getLocation().getBlockX() != e.getBlockPlaced().getLocation().getBlockX()) {
				continue;
			}
			if(fallingBlock.getLocation().getBlockZ() != e.getBlockPlaced().getLocation().getBlockZ()) {
				continue;
			}
			if(fallingBlock.getLocation().getBlockY() < e.getBlockPlaced().getLocation().getBlockY()) {
				continue;
			}
			
			if(plugin.treeUtils.isPartOfTree(fallingBlock.getBlockData().getMaterial())) {
				e.setCancelled(true);
			}
			
		}
	}

}
