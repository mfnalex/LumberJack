package de.jeffclan.LumberJack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

	LumberJack plugin;

	public BlockBreakListener(LumberJack plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {

		if (plugin.isPartOfTree(e.getBlock())) {

			// Dont show message when gravity is forced
			if (!e.getPlayer().hasPermission("lumberjack.force") && e.getPlayer().hasPermission("lumberjack.use")) {
				Player p = e.getPlayer();
				if (!plugin.getPlayerSetting(p).gravityEnabled) {
					if (!plugin.getPlayerSetting(p).hasSeenMessage) {
						plugin.getPlayerSetting(p).hasSeenMessage = true;
						if (plugin.getConfig().getBoolean("show-message-when-breaking-log")) {
							p.sendMessage(plugin.messages.MSG_COMMANDMESSAGE);
						}
					}
					return;
				} else {
					if (!plugin.getPlayerSetting(p).hasSeenMessage) {
						plugin.getPlayerSetting(p).hasSeenMessage = true;
						if (plugin.getConfig().getBoolean("show-message-when-breaking-log-and-gravity-is-enabled")) {
							p.sendMessage(plugin.messages.MSG_COMMANDMESSAGE2);
						}
					}
				}
			}
		}

		if (!plugin.isPartOfTree(e.getBlock())) {
			return;
		}

		if (!plugin.getPlayerSetting(e.getPlayer()).gravityEnabled
				&& !e.getPlayer().hasPermission("lumberjack.force")) {
			return;
		}

		ArrayList<Block> logs;

		// Atached logs fall down
		if (plugin.getConfig().getBoolean("attached-logs-fall-down")) {

			logs = new ArrayList<Block>();
			plugin.getTreeTrunk(e.getBlock().getRelative(BlockFace.UP), logs);
			logs.remove(e.getBlock());
			
			Collections.sort(logs, new Comparator<Block>() {
			    public int compare(Block b1, Block b2) {
			        if( b1.getY() < b2.getY()) return -1;
			        if(b1.getY() > b2.getY()) return 1;
			        return 0;
			    }
			});
			
			/*for(Block b:logs) {
				System.out.println(b.getLocation().toString());
			}*/

		} else {

			logs = new ArrayList<Block>(Arrays.asList(plugin.getLogsAbove(e.getBlock())));

		}

		// I have really no idea what exactly I did here. There was a problem with falling Blocks being spawned isntead of logs
		// that were on the ground, so they broke immediately and dropped themself. I think I fixed this by the following line
		// if(logAbove.getRelative(BlockFace.DOWN).getType() == Material.AIR || logs.contains(logAbove) || logs.contains(logAbove.getRelative(BlockFace.DOWN))) {
		for (Block logAbove : logs) {
			if(logAbove.getRelative(BlockFace.DOWN).getType() == Material.AIR || logs.contains(logAbove) || logs.contains(logAbove.getRelative(BlockFace.DOWN))) {
				
				BlockData blockData = logAbove.getBlockData().clone();
				logAbove.setType(Material.AIR);
				logAbove.getLocation().getWorld().spawnFallingBlock(logAbove.getLocation().add(plugin.fallingBlockOffset),
						blockData);
				
			}
			
		}

	}

}