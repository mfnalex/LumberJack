package de.jeff_media.lumberjack;

import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CustomDropManager {

    private final LumberJack plugin = LumberJack.getInstance();
    private final HashMap<Material, List<CustomDrop>> customDrops = new HashMap<>();
    //private final HashMap<Material, List<CustomDrop>> strippingDrops = new HashMap<>();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public void doCustomDrops(Location loc, Material mat) {
        if(!customDrops.containsKey(mat)) return;
        List<CustomDrop> drops = customDrops.get(mat);
        for(CustomDrop drop : drops) {
            double chance = random.nextDouble(0,100);
            if(chance <= drop.chance) {
                loc.getWorld().dropItemNaturally(loc, new ItemStack(drop.mat));
            }
        }
    }

    public CustomDropManager() {
        File file = new File(plugin.getDataFolder(), "custom-drops.yml");
        plugin.saveResource("custom-drops.example.yml", true);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for(String leafType : yaml.getKeys(false)) {
            Material baseMat = Enums.getIfPresent(Material.class, leafType.toUpperCase(Locale.ROOT)).orNull();
            if(baseMat == null) continue;
            List<CustomDrop> list = new ArrayList<>();
            for(String drop : yaml.getStringList(leafType)) {
                String[] parsed = drop.split("%");
                try {
                    double chance = Double.parseDouble(parsed[0]);
                    Material dropType = Enums.getIfPresent(Material.class, parsed[1].toUpperCase(Locale.ROOT)).orNull();
                    if(dropType == null) {
                        plugin.getLogger().warning("Invalid block name in custom-drops.yml: " + parsed[1]);
                        continue;
                    }
                    list.add(new CustomDrop(chance, dropType));
                } catch (Throwable t) {
                    plugin.getLogger().warning("Invalid line while parsing custom-drops.yml: " + drop);
                }
            }
            customDrops.put(baseMat, list);
        }

        if(yaml.isConfigurationSection("stripping")) {
            for (String log : yaml.getConfigurationSection("stripping").getKeys(false)) {
                Material baseType = Enums.getIfPresent(Material.class, log.toUpperCase(Locale.ROOT)).orNull();
                if(baseType==null) continue;
                List<CustomDrop> list = new ArrayList<>();
                for(String drop : yaml.getStringList("stripping."+log)) {
                    String[] parsed = drop.split("%");
                    try {
                        double chance = Double.parseDouble(parsed[0]);
                        Material dropType = Enums.getIfPresent(Material.class, parsed[1].toUpperCase(Locale.ROOT)).orNull();
                        if(dropType == null) {
                            plugin.getLogger().warning("Invalid block name in custom-drops.yml: " + parsed[1]);
                            continue;
                        }
                        list.add(new CustomDrop(chance, dropType));
                    } catch (Throwable t) {
                        plugin.getLogger().warning("Invalid line while parsing custom-drops.yml: " + drop);
                    }
                }
                customDrops.put(baseType,list);
            }
        }
    }

    static class CustomDrop {

        private final double chance;
        private final Material mat;

        CustomDrop(double chance, Material drop) {
            this.chance = chance;
            this.mat = drop;
        }

    }

}
