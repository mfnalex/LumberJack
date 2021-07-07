package de.jeff_media.lumberjack.listeners;

import de.jeff_media.jefflib.BlockTracker;
import de.jeff_media.lumberjack.LumberJack;
import de.jeff_media.lumberjack.NBTKeys;
import de.jeff_media.nbtapi.NBTAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final LumberJack plugin;

    public BlockPlaceListener(LumberJack plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLogStrip(BlockPlaceEvent event) {
        String typeName = event.getBlock().getType().name();
        if(!(typeName.endsWith("_LOG") || typeName.endsWith("_WOOD") || typeName.endsWith("_STEM") || typeName.endsWith("_HYPHAE"))) return;
        if(!BlockTracker.isTrackedBlockType(event.getBlock().getType())) return;
        if(BlockTracker.isPlayerPlacedBlock(event.getBlock())) return;
        final Block placedBlock = event.getBlock();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(placedBlock.getType().name().startsWith("STRIPPED_")) {
                BlockTracker.setPlayerPlacedBlock(placedBlock, false);
                plugin.getCustomDropManager().doCustomDrops(placedBlock.getLocation(), placedBlock.getType());
            }
        },1L);
    }


    // Prevent torches and stuff being placed inside a falling log
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {

        if (!plugin.getConfig().getBoolean("prevent-torch-exploit")) {
            return;
        }

        //System.out.println("possible conflicting block has been placed");

        for (Entity entity : e.getBlock().getLocation().getWorld().getNearbyEntities(e.getBlock().getLocation(), 1, 256, 1, entity -> entity instanceof FallingBlock)) {

            FallingBlock fallingBlock = (FallingBlock) entity;

            if (!NBTAPI.hasNBT(fallingBlock, NBTKeys.IS_FALLING_LOG)) {
                continue;
            }

            if (fallingBlock.getLocation().getBlockX() != e.getBlockPlaced().getLocation().getBlockX()) {
                continue;
            }
            if (fallingBlock.getLocation().getBlockZ() != e.getBlockPlaced().getLocation().getBlockZ()) {
                continue;
            }
            if (fallingBlock.getLocation().getBlockY() < e.getBlockPlaced().getLocation().getBlockY()) {
                continue;
            }

            //if(plugin.treeUtils.isPartOfTree(fallingBlock.getBlockData().getMaterial())) {
            e.setCancelled(true);
            e.getPlayer().updateInventory();
            //}

        }
    }

}
