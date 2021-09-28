package de.jeff_media.lumberjack;

import com.google.common.base.Enums;
import de.jeff_media.jefflib.BlockTracker;
import de.jeff_media.jefflib.JeffLib;
import de.jeff_media.jefflib.McVersion;
import de.jeff_media.jefflib.pluginhooks.PlaceholderAPIUtils;
import de.jeff_media.lumberjack.commands.CommandLumberjack;
import de.jeff_media.lumberjack.config.ConfigUpdater;
import de.jeff_media.lumberjack.config.Messages;
import de.jeff_media.lumberjack.data.PlayerSetting;
import de.jeff_media.lumberjack.listeners.BlockBreakListener;
import de.jeff_media.lumberjack.listeners.BlockPlaceListener;
import de.jeff_media.lumberjack.listeners.DecayListener;
import de.jeff_media.lumberjack.listeners.PlayerListener;
import de.jeff_media.lumberjack.utils.TreeUtils;
import de.jeff_media.updatechecker.UpdateChecker;
import de.jeff_media.updatechecker.UserAgentBuilder;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;


public class LumberJack extends JavaPlugin {

    //static final String[] woodTypes = {"acacia", "birch", "jungle", "oak", "dark_oak", "spruce"};
    static final String[] treeBlocks = {
            "ACACIA_LOG", "STRIPPED_ACACIA_LOG", "ACACIA_WOOD", "STRIPPED_ACACIA_WOOD",
            "BIRCH_LOG", "STRIPPED_BIRCH_LOG", "BIRCH_WOOD", "STRIPPED_BIRCH_WOOD",
            "DARK_OAK_LOG", "STRIPPED_DARK_OAK_LOG", "DARK_OAK_WOOD", "STRIPPED_DARK_OAK_WOOD",
            "JUNGLE_LOG", "STRIPPED_JUNGLE_LOG", "JUNGLE_WOOD", "STRIPPED_JUNGLE_WOOD",
            "OAK_LOG", "STRIPPED_OAK_LOG", "OAK_WOOD", "STRIPPED_OAK_WOOD",
            "SPRUCE_LOG", "STRIPPED_SPRUCE_LOG", "SPRUCE_WOOD", "STRIPPED_SPRUCE_WOOD",
            "WARPED_STEM", "STRIPPED_WARPED_STEM", "WARPED_HYPHAE", "STRIPPED_WARPED_HYPHAE",
            "CRIMSON_STEM", "STRIPPED_CRIMSON_STEM", "CRIMSON_HYPHAE", "STRIPPED_CRIMSON_HYPHAE"
    };
    private static final int SPIGOT_RESOURCE_ID = 60306;
    private static LumberJack instance;
    public final Vector fallingBlockOffset = new Vector(0.5, 0.0, 0.5);
    public final int maxTreeSize = 50;
    @SuppressWarnings("FieldCanBeLocal")
    private final int currentConfigVersion = 14;
    public TreeUtils treeUtils;
    public Messages messages;
    public ArrayList<String> disabledWorlds;
    public ArrayList<String> treeBlockNames;
    boolean gravityEnabledByDefault = false;
    HashMap<Player, PlayerSetting> perPlayerSettings;
    boolean debug = false;
    private boolean usingMatchingConfig = true;
    private CustomDropManager customDropManager;
    public final Set<Integer> decayTasks = new HashSet<>();

    public CustomDropManager getCustomDropManager() {
        return customDropManager;
    }

    public HashSet<BukkitTask> getScheduledTasks() {
        return scheduledTasks;
    }

    private final HashSet<BukkitTask> scheduledTasks = new HashSet<>();
    //ArrayList<String> treeGroundBlockNames;

    public static LumberJack getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        if (McVersion.getMinor() < 14) {
            getLogger().severe("LumberJack requires AT LEAST Minecraft version 1.14.1!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        JeffLib.init(this);

        PlaceholderAPIUtils.register("enabled", (player) -> {
            if(!player.isOnline()) return "false";
            return String.valueOf(getPlayerSetting(player.getPlayer()).gravityEnabled);
        });

        customDropManager = new CustomDropManager();

        createConfig();
        //treeBlockNames = (ArrayList<String>) getConfig().getStringList("tree-blocks");

        treeBlockNames = new ArrayList<>();
        treeBlockNames.addAll(Arrays.asList(treeBlocks));

        //treeGroundBlockNames = (ArrayList<String>) getConfig().getStringList("tree-ground-blocks");
        messages = new Messages(this);
        treeUtils = new TreeUtils(this);
        BlockBreakListener blockBreakListener = new BlockBreakListener(this);
        BlockPlaceListener blockPlaceListener = new BlockPlaceListener(this);
        DecayListener decayListener = new DecayListener();
        PlayerListener playerListener = new PlayerListener(this);
        CommandLumberjack commandLumberjack = new CommandLumberjack(this);
        Objects.requireNonNull(getCommand("lumberjack")).setExecutor(commandLumberjack);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        getServer().getPluginManager().registerEvents(blockPlaceListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(decayListener, this);

        perPlayerSettings = new HashMap<>();

        gravityEnabledByDefault = getConfig().getBoolean("gravity-enabled-by-default");

        Metrics metrics = new Metrics(this, 3184);
        metrics.addCustomChart(new Metrics.SimplePie("gravity_enabled_by_default", () -> Boolean.toString(getConfig().getBoolean("gravity-enabled-by-default"))));
        metrics.addCustomChart(new Metrics.SimplePie("using_matching_config", () -> Boolean.toString(usingMatchingConfig)));
        metrics.addCustomChart(new Metrics.SimplePie("show_message_again_after_logout", () -> Boolean.toString(getConfig().getBoolean("show-message-again-after-logout"))));
        metrics.addCustomChart(new Metrics.SimplePie("attached_logs_fall_down", () -> Boolean.toString(getConfig().getBoolean("attached-logs-fall-down"))));
        metrics.addCustomChart(new Metrics.SimplePie("prevent_torch_exploit", () -> Boolean.toString(getConfig().getBoolean("prevent-torch-exploit"))));

        UpdateChecker updateChecker = UpdateChecker.init(this, "https://api.jeff-media.de/lumberjack/latest-version.txt")
                .setChangelogLink(SPIGOT_RESOURCE_ID)
                .setDownloadLink(SPIGOT_RESOURCE_ID)
                .setDonationLink("https://paypal.me/mfnalex")
                .setUserAgent(UserAgentBuilder.getDefaultUserAgent());
        if (Objects.requireNonNull(getConfig().getString("check-for-updates", "true")).equalsIgnoreCase("true")) {
            updateChecker.checkNow().checkEveryXHours(getConfig().getDouble("check-interval"));
        } // When set to on-startup, we check right now (delay 0)
        else if (Objects.requireNonNull(getConfig().getString("check-for-updates", "true")).equalsIgnoreCase("on-startup")) {
            updateChecker.checkNow();
        }

        trackBlocks();
    }

    private void trackBlocks() {
        // Track all player placed logs
        Collection<Material> trackedBlocks = new HashSet<>();
        for (String name : treeBlockNames) {
            Material mat = Enums.getIfPresent(Material.class, name).orNull();
            if (mat != null) {
                trackedBlocks.add(mat);
                //System.out.println("Tracking material " + mat.name());
            }
        }
        BlockTracker.addTrackedBlockTypes(trackedBlocks);
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
        getConfig().addDefault("fast-leaves-decay", false);
        getConfig().addDefault("fast-leaves-decay-duration", 10);
        getConfig().addDefault("only-natural-logs", true);

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
        for(BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
            if(task.getOwner() == this) {
                task.cancel();
            }
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
                    p.getUniqueId() + ".yml");
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
                    p.getUniqueId() + ".yml");
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

    public void reload() {
        reloadConfig();
        customDropManager = new CustomDropManager();
    }
}
	

