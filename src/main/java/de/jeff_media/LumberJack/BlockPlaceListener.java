package de.jeff_media.LumberJack;

import de.jeff_media.nbtapi.NBTAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Objects;
import java.util.function.Predicate;

public class BlockPlaceListener implements Listener {
	
	private final LumberJack plugin;
	
	BlockPlaceListener(LumberJack plugin) {
		this.plugin=plugin;
	}
	
	
	// Prevent torches and stuff being placed inside a falling log
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		
		if(!plugin.getConfig().getBoolean("prevent-torch-exploit")) {
			return;
		}
		
		//System.out.println("possible conflicting block has been placed");
		
		for(Entity entity : e.getBlock().getLocation().getWorld().getNearbyEntities(e.getBlock().getLocation(), 0, 256, 0, new Predicate<Entity>() {
			@Override
			public boolean test(Entity entity) {
				return entity instanceof FallingBlock;
			}
		})) {
			
			//System.out.println(entity.getType().name());
			
			//System.out.println(("falling block detected while block was placed"));
			
			FallingBlock fallingBlock = (FallingBlock) entity;

			if(!NBTAPI.hasNBT(fallingBlock,NBTKeys.IS_FALLING_LOG)) {
				continue;
			}
			
			if(fallingBlock.getLocation().getBlockX() != e.getBlockPlaced().getLocation().getBlockX()) {
				continue;
			}
			if(fallingBlock.getLocation().getBlockZ() != e.getBlockPlaced().getLocation().getBlockZ()) {
				continue;
			}
			if(fallingBlock.getLocation().getBlockY() < e.getBlockPlaced().getLocation().getBlockY()) {
				continue;
			}
			
			//if(plugin.treeUtils.isPartOfTree(fallingBlock.getBlockData().getMaterial())) {
			e.setCancelled(true);
			//}
			
		}
	}

}
