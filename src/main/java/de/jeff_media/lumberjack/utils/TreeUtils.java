package de.jeff_media.lumberjack.utils;

import com.google.common.base.Enums;
import de.jeff_media.lumberjack.LumberJack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;

public class TreeUtils {

    private final LumberJack main;

    public TreeUtils(LumberJack main) {
        this.main = main;
    }

    static Material[] getValidGroundTypes(Material mat) {
        switch (mat.name().replace("STRIPPED_","").replace("_WOOD","_LOG").replace("_STEM","_HYPHAE")) {
            case "ACACIA_LOG":
            case "BIRCH_LOG":
            case "DARK_OAK_LOG":
            case "JUNGLE_LOG":
            case "OAK_LOG":
            case "SPRUCE_LOG":
                return new Material[]{
                        Material.DIRT,
                        Material.GRASS_BLOCK,
                        Material.MYCELIUM,
                        Material.COARSE_DIRT,
                        Material.PODZOL,
                        Enums.getIfPresent(Material.class, "ROOTED_DIRT").or(Material.DIRT),
                        Enums.getIfPresent(Material.class, "MOSS_BLOCK").or(Material.DIRT)}; // TODO: Fuck the duplicates
            case "CRIMSON_STEM":
                return new Material[]{
                        Material.CRIMSON_NYLIUM,
                        Material.NETHERRACK
                };
            case "WARPED_STEM":
                return new Material[]{
                        Material.WARPED_NYLIUM,
                        Material.NETHERRACK
                };
        }

        return null;
    }

    static boolean matchesTree(Material orig, Material now) {
        switch (now) {
            case ACACIA_LOG:
            case ACACIA_LEAVES:
            case STRIPPED_ACACIA_LOG:
                return orig == Material.ACACIA_LOG || orig == Material.STRIPPED_ACACIA_LOG;
            case BIRCH_LOG:
            case BIRCH_LEAVES:
            case STRIPPED_BIRCH_LOG:
                return orig == Material.BIRCH_LOG || orig == Material.STRIPPED_BIRCH_LOG;
            case DARK_OAK_LOG:
            case DARK_OAK_LEAVES:
            case STRIPPED_DARK_OAK_LOG:
                return orig == Material.DARK_OAK_LOG || orig == Material.STRIPPED_DARK_OAK_LOG;
            case JUNGLE_LOG:
            case JUNGLE_LEAVES:
            case STRIPPED_JUNGLE_LOG:
                return orig == Material.JUNGLE_LOG || orig == Material.STRIPPED_JUNGLE_LOG;
            case OAK_LOG:
            case OAK_LEAVES:
            case STRIPPED_OAK_LOG:
                return orig == Material.OAK_LOG || orig == Material.STRIPPED_OAK_LOG;
            case SPRUCE_LOG:
            case SPRUCE_LEAVES:
            case STRIPPED_SPRUCE_LOG:
                return orig == Material.SPRUCE_LOG || orig == Material.STRIPPED_SPRUCE_LOG;
        }
        switch (now.name()) {
            case "WARPED_STEM":
            case "STRIPPED_WARPED_STEM":
                return orig.name().equals("WARPED_STEM") || orig.name().equals("STRIPPED_WARPED_STEM");
            case "CRIMSON_STEM":
            case "STRIPPED_CRIMSON_STEM":
                return orig.name().equals("CRIMSON_STEM") || orig.name().equals("STRIPPED_CRIMSON_STEM");
        }
        return false;
    }

    public static boolean matchesTrunkType(Material mat, Material mat2) {
        if(mat==mat2) return true;
        String n1 = mat.name().replace("STRIPPED_","").replace("_WOOD","_LOG");
        String n2 = mat2.name().replace("STRIPPED_","").replace("_WOOD","_LOG");
        return n1.equals(n2);
    }

    public static boolean isAboveNonSolidBlock(Block block) {

        for (int height = block.getY() - 1; height >= 0; height--) {
            Block candidate = block.getWorld().getBlockAt(block.getX(), height, block.getZ());
            if (candidate.getType().isSolid()) {
                return true;
            }
            if (candidate.getType() != Material.AIR) {
                return false;
            }

        }
        return true;
    }

    static String getFlavor(Material mat) {
        String name = mat.name().toLowerCase().replace("_WOOD","_LOG").replace("_STEM","_LOG").replace("_HYPHAE","_LOG");
        if(!name.contains("_LOG")) return "none";
        if (name.contains("acacia")) {
            return "acacia";
        } else if (name.contains("birch")) {
            return "birch";
        } else if (name.contains("dark_oak")) {
            return "dark_oak";
        } else if (name.contains("oak")) {
            return "oak";
        } else if (name.contains("jungle")) {
            return "jungle";
        } else if (name.contains("spruce")) {
            return "spruce";
        } else {
            return "none";
        }

    }

    static ArrayList<Block> getAdjacent(Block block) {
        ArrayList<Block> blocks = new ArrayList<>();
        Block above = block.getRelative(BlockFace.UP);
        Material mat = block.getType();

        final BlockFace[] faces = {BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.EAST, BlockFace.NORTH_EAST,
                BlockFace.NORTH, BlockFace.NORTH_WEST, BlockFace.WEST, BlockFace.SOUTH_WEST};

        if (matchesTrunkType(above.getType(),mat)) {
            blocks.add(above);
        }

        for (BlockFace face : faces) {
            if (block.getRelative(face).getType() == mat) blocks.add(block.getRelative(face));
        }

        for (BlockFace face : faces) {
            if (above.getRelative(face).getType() == mat) blocks.add(above.getRelative(face));
        }
        //blocks.forEach((b) -> System.out.println("  "+b.getType()+"@"+b.getLocation()));

        return blocks;
    }

    public static void getTreeTrunk2(Block block, ArrayList<Block> list, Material mat) {
        if (!matchesTrunkType(mat, block.getType())) return;
        if (!list.contains(block)) {
            list.add(block);
            //System.out.println("adding "+block.getType().name()+"@"+block.getLocation());

            for (Block next : getAdjacent(block)) {
                getTreeTrunk2(next, list, mat);
            }
        }
    }

    public boolean isPartOfTree(Block block) {
        return isPartOfTree(block.getType());
    }

    public boolean isOnTreeGround(Block block) {

        int maxAirInBetween = main.getConfig().getInt("max-air-in-trunk");
        int airInBetween = 0;
        Block currentBlock = block;

        while (isPartOfTree(currentBlock) || currentBlock.getType().isAir()) {

            if (currentBlock.getType().isAir()) {
                airInBetween++;
                if (airInBetween > maxAirInBetween) {
                    return false;
                }
            }

            currentBlock = currentBlock.getRelative(BlockFace.DOWN);
        }

        for (Material mat : getValidGroundTypes(block.getType())) {
            if (mat == currentBlock.getType()) return true;
        }
        return false;
    }

    boolean isPartOfTree(Material mat) {

        for (String blockName : main.treeBlockNames) {
            if (Material.matchMaterial(blockName) != null) {
                if (Material.matchMaterial(blockName) == mat) {
                    return true;
                }
            } //else {
            //main.getLogger().warning("Block type not found: " + blockName);
            // TODO: Build list of Materials only once, then cache it to avoid String->Material conversion on every block break
            //}
        }

        return false;
    }

    public Block[] getLogsAbove(Block block) {
        String flavor = getFlavor(block.getType());
        ArrayList<Block> list = new ArrayList<>();
        Block currentBlock = block.getRelative(BlockFace.UP);
        while (isPartOfTree(currentBlock) && list.size() < main.maxTreeSize && getFlavor(currentBlock.getType()).equalsIgnoreCase(flavor)) {
            list.add(currentBlock);
            currentBlock = currentBlock.getRelative(BlockFace.UP);
        }
        return list.toArray(new Block[0]);
    }
}
