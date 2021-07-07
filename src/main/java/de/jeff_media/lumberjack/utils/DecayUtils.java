package de.jeff_media.lumberjack.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Leaves;

import java.util.Collection;
import java.util.HashSet;

public class DecayUtils {

    private static final int MAX_DISTANCE = 6;
    private static final int RADIUS = 3;
    private static final boolean IS_AT_LEAST_v1_17;
    private static Boolean tagsAvailable = null;

    static {
        boolean azaleaLeavesAvailable = false;
        try {
            //noinspection ResultOfMethodCallIgnored
            Material.FLOWERING_AZALEA_LEAVES.getData();
            azaleaLeavesAvailable = true;
        } catch (Throwable ignored) {

        }
        IS_AT_LEAST_v1_17 = azaleaLeavesAvailable;
    }

    public static Collection<Block> getLeaves(BlockState originalLeaf) {
        Collection<Block> blocks = new HashSet<>();
        int blockX = originalLeaf.getX();
        int blockY = originalLeaf.getY();
        int blockZ = originalLeaf.getZ();
        World world = originalLeaf.getWorld();
        for (int x = blockX - RADIUS; x <= blockX + RADIUS; x++) {
            for (int y = blockY - RADIUS; y <= blockY + RADIUS; y++) {
                for (int z = blockZ - RADIUS; z <= blockZ + RADIUS; z++) {
                    Block candidate = world.getBlockAt(x, y, z);
                    if(candidate==null) continue;
                    if (candidate.getType().isAir()) continue;
                    if (!isLeaf(candidate)) {
                        continue;
                    }
                    if (!isMatchingLeaf(originalLeaf.getType(), candidate.getType())) {
                        continue;
                    }
                    Leaves leaves = (Leaves) candidate.getBlockData();
                    if (leaves.isPersistent()) {
                        continue;
                    }
                    if (leaves.getDistance() <= MAX_DISTANCE) {
                        continue;
                    }
                    blocks.add(candidate);
                }
            }
        }
        return blocks;
    }

    public static boolean isLeaf(Block block) {
        return isLeaf(block.getType());
    }

    private static boolean isLeaf(Material material) {
        if (tagsAvailable == null) {
            try {
                Tag.LEAVES.isTagged(material);
                tagsAvailable = true;
            } catch (Throwable t) {
                tagsAvailable = false;
            }
        }

        if (tagsAvailable) {
            return Tag.LEAVES.isTagged(material);
        } else {
            return material.name().endsWith("_LEAVES");
        }

    }

    private static boolean isMatchingLeaf(Material leaf1, Material leaf2) {
        if (IS_AT_LEAST_v1_17) {
            switch (leaf1) {
                case AZALEA_LEAVES:
                case FLOWERING_AZALEA_LEAVES:
                    return leaf2 == Material.AZALEA_LEAVES || leaf2 == Material.FLOWERING_AZALEA_LEAVES;
                default:
                    return leaf1 == leaf2;
            }
        }
        return leaf1 == leaf2;
    }

}
