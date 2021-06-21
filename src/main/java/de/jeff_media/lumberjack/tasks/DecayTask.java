package de.jeff_media.lumberjack.tasks;

import de.jeff_media.jefflib.Ticks;
import de.jeff_media.lumberjack.LumberJack;
import de.jeff_media.lumberjack.utils.DecayUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Random;

public class DecayTask extends BukkitRunnable {

    private static final LumberJack plugin = LumberJack.getInstance();
    private static final Random rand = new Random();
    private final BlockState leaf;

    public DecayTask(BlockState leaf) {
        this.leaf = leaf;
    }

    @Override
    public void run() {
        Collection<Block> leaves = DecayUtils.getLeaves(leaf);
        for (Block leaf : leaves) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (DecayUtils.isLeaf(leaf)) {
                    Bukkit.getScheduler().runTask(plugin, (Runnable) leaf::breakNaturally);
                }
            }, rand.nextInt((int) Ticks.fromSeconds(plugin.getConfig().getDouble("fast-leaves-decay-duration"))));
        }
    }
}
