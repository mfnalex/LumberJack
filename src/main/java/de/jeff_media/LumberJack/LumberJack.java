package de.jeff_media.LumberJack;

import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class LumberJack extends JavaPlugin {

    //static final String[] woodTypes = {"acacia", "birch", "jungle", "oak", "dark_oak", "spruce"};
    static final String[] treeBlocks = {
            "ACACIA_LOG","STRIPPED_ACACIA_LOG",
            "BIRCH_LOG","STRIPPED_BIRCH_LOG",
            "DARK_OAK_LOG","STRIPPED_DARK_OAK_LOG",
            "JUNGLE_LOG","STRIPPED_JUNGLE_LOG",
            "OAK_LOG","STRIPPED_OAK_LOG",
            "SPRUCE_LOG","STRIPPED_SPRUCE_LOG",
            "WARPED_STEM","STRIPPED_WARPED_STEM",
            "CRIMSON_STEM","STRIPPED_CRIMSON_STEM"
    };
    TreeUtils treeUtils;
    int maxTreeSize = 50;
    final Vector fallingBlockOffset = new Vector(0.5, 0.0, 0.5);
    boolean gravityEnabledByDefault = false;
    Messages messages;
    PluginUpdateChecker updateChecker;
    ArrayList<String> disabledWorlds;
    HashMap<Player, PlayerSetting> perPlayerSettings;
    private final int currentConfigVersion = 9;
    private boolean usingMatchingConfig = true;
    private int updateCheckInterval = 86400; //one day
    boolean debug = false;
    ArrayList<String> treeBlockNames;
    //ArrayList<String> treeGroundBlockNames;


    @Override
    public void onEnable() {

        createConfig();
        //treeBlockNames = (ArrayList<String>) getConfig().getStringList("tree-blocks");

        treeBlockNames = new ArrayList<>();
        treeBlockNames.addAll(Arrays.asList(treeBlocks));

        //treeGroundBlockNames = (ArrayList<String>) getConfig().getStringList("tree-ground-blocks");
        messages = new Messages(this);
        if(updateChecker != null) {
            updateChecker.stop();
        }
        updateChecker = new PluginUpdateChecker(this, "https://api.jeff-media.de/lumberjack/lumberjack-latest-version.txt", "https://www.spigotmc.org/resources/1-13-1-16-lumberjack.60306/", "https://github.com/JEFF-Media-GbR/Spigot-LumberJack/blob/master/CHANGELOG.md", "https://chestsort.de/donate");
        updateCheckInterval = (int) (getConfig().getDouble("check-interval")*60*60);
        treeUtils = new TreeUtils(this);
        BlockBreakListener blockBreakListener = new BlockBreakListener(this);
        BlockPlaceListener blockPlaceListener = new BlockPlaceListener(this);
        PlayerListener playerListener = new PlayerListener(this);
        CommandLumberjack commandLumberjack = new CommandLumberjack(this);
        Objects.requireNonNull(getCommand("lumberjack")).setExecutor(commandLumberjack);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        getServer().getPluginManager().registerEvents(blockPlaceListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        perPlayerSettings = new HashMap<>();

        gravityEnabledByDefault = getConfig().getBoolean("gravity-enabled-by-default");

        Metrics metrics = new Metrics(this,3184);
        metrics.addCustomChart(new Metrics.SimplePie("gravity_enabled_by_default", () -> Boolean.toString(getConfig().getBoolean("gravity-enabled-by-default"))));
        metrics.addCustomChart(new Metrics.SimplePie("using_matching_config", () -> Boolean.toString(usingMatchingConfig)));
        metrics.addCustomChart(new Metrics.SimplePie("show_message_again_after_logout", () -> Boolean.toString(getConfig().getBoolean("show-message-again-after-logout"))));
        metrics.addCustomChart(new Metrics.SimplePie("attached_logs_fall_down", () -> Boolean.toString(getConfig().getBoolean("attached-logs-fall-down"))));
        metrics.addCustomChart(new Metrics.SimplePie("prevent_torch_exploit", () -> Boolean.toString(getConfig().getBoolean("prevent-torch-exploit"))));

        if (Objects.requireNonNull(getConfig().getString("check-for-updates", "true")).equalsIgnoreCase("true")) {
            updateChecker.check(updateCheckInterval);
        } // When set to on-startup, we check right now (delay 0)
        else if (Objects.requireNonNull(getConfig().getString("check-for-updates", "true")).equalsIgnoreCase("on-startup")) {
            updateChecker.check();
        }
    }

    private void showOldConfigWarning() {
        getLogger().warning("==============================================");
        getLogger().warning("You were using an old config file. LumberJack");
        getLogger().warning("has updated the file to the newest version.");
        getLogger().warning("Your changes have been kept.");
        getLogger().warning("==============================================");
    }

    private void createConfig() {
        saveDefaultConfig();

        if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
            showOldConfigWarning();
            ConfigUpdater configUpdater = new ConfigUpdater(this);
            configUpdater.updateConfig();
            usingMatchingConfig = true;
            //createConfig();
        }

        File playerDataFolder = new File(getDataFolder().getPath() + File.separator + "playerdata");
        if (!playerDataFolder.getAbsoluteFile().exists()) {
            playerDataFolder.mkdir();
        }

        getConfig().addDefault("gravity-enabled-by-default", false);
        getConfig().addDefault("check-for-updates", "true");
        getConfig().addDefault("show-message-again-after-logout", true);
        getConfig().addDefault("attached-logs-fall-down", true);
        getConfig().addDefault("prevent-torch-exploit", true);
        getConfig().addDefault("must-use-axe", true);
        getConfig().addDefault("max-air-in-trunk", 1);

        // Load disabled-worlds. If it does not exist in the config, it returns null. That's no problem
        disabledWorlds = (ArrayList<String>) getConfig().getStringList("disabled-worlds");

    }

    public PlayerSetting getPlayerSetting(Player p) {
        registerPlayer(p);
        return perPlayerSettings.get(p);
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            unregisterPlayer(p);
        }
    }

    public void togglePlayerSetting(Player p) {
        registerPlayer(p);
        boolean enabled = perPlayerSettings.get(p).gravityEnabled;
        perPlayerSettings.get(p).gravityEnabled = !enabled;
    }

    public void registerPlayer(Player p) {
        if (!perPlayerSettings.containsKey(p)) {

            File playerFile = new File(getDataFolder() + File.separator + "playerdata",
                    p.getUniqueId().toString() + ".yml");
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

            boolean activeForThisPlayer;

            if (!playerFile.exists()) {
                activeForThisPlayer = gravityEnabledByDefault;
            } else {
                activeForThisPlayer = playerConfig.getBoolean("gravityEnabled");
            }

            PlayerSetting newSettings = new PlayerSetting(activeForThisPlayer);
            if (!getConfig().getBoolean("show-message-again-after-logout")) {
                newSettings.hasSeenMessage = playerConfig.getBoolean("hasSeenMessage");
            }
            perPlayerSettings.put(p, newSettings);
        }
    }

    public void unregisterPlayer(Player p) {
        if (perPlayerSettings.containsKey(p)) {
            PlayerSetting setting = getPlayerSetting(p);
            File playerFile = new File(getDataFolder() + File.separator + "playerdata",
                    p.getUniqueId().toString() + ".yml");
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            playerConfig.set("gravityEnabled", setting.gravityEnabled);
            playerConfig.set("hasSeenMessage", setting.hasSeenMessage);
            try {
                playerConfig.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            perPlayerSettings.remove(p);
        }
    }
}
	

