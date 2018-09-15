package de.jeffclan.LumberJack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;


public class LumberJack extends JavaPlugin {

	static final String[] woodTypes = { "acacia", "birch", "jungle", "oak", "dark_oak", "spruce" };
	int maxTreeSize = 50;
	Vector fallingBlockOffset = new Vector(0.5, 0.0, 0.5);
	boolean gravityEnabledByDefault = false;
	Messages messages;
	UpdateChecker updateChecker;
	private int currentConfigVersion = 3;
	private boolean usingMatchingConfig = true;

	HashMap<Player, PlayerSetting> perPlayerSettings;
	private int updateCheckInterval = 86400; //one day
	
	private ArrayList<String> treeBlockNames;
	

	@Override
	public void onEnable() {
		
		

		createConfig();
		treeBlockNames = (ArrayList<String>) getConfig().getStringList("tree-blocks");
		
		
		messages = new Messages(this);
		updateChecker = new UpdateChecker(this);
		BlockBreakListener blockBreakListener = new BlockBreakListener(this);
		PlayerListener playerListener = new PlayerListener(this);
		CommandLumberjack commandLumberjack = new CommandLumberjack(this);
		getCommand("lumberjack").setExecutor(commandLumberjack);
		getServer().getPluginManager().registerEvents(blockBreakListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);

		perPlayerSettings = new HashMap<Player, PlayerSetting>();
		
		gravityEnabledByDefault = getConfig().getBoolean("gravity-enabled-by-default");

		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SimplePie("gravity_enabled_by_default", () -> Boolean.toString(getConfig().getBoolean("gravity-enabled-by-default"))));
		metrics.addCustomChart(new Metrics.SimplePie("using_matching_config", () -> Boolean.toString(usingMatchingConfig)));
		metrics.addCustomChart(new Metrics.SimplePie("show_message_again_after_logout", () -> Boolean.toString(getConfig().getBoolean("show-message-again-after-logout"))));
		metrics.addCustomChart(new Metrics.SimplePie("attached_logs_fall_down", () -> Boolean.toString(getConfig().getBoolean("attached-logs-fall-down"))));

		
		if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("true")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					updateChecker.checkForUpdate();
				}
			}, 0L, updateCheckInterval  * 20);
		} else if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("on-startup")) {
			updateChecker.checkForUpdate();
		}
	}

	private void createConfig() {
		saveDefaultConfig();
		
		if (getConfig().getInt("config-version", 0) < 3) {
			getLogger().warning("========================================================");
			getLogger().warning("You are using a config file that has been generated");
			getLogger().warning("prior to LumberJack version 1.1.0.");
			getLogger().warning("To allow everyone to use the new features, your config");
			getLogger().warning("has been renamed to config.old.yml and a new one has");
			getLogger().warning("been generated. Please examine the new config file to");
			getLogger().warning("see the new possibilities and adjust your settings.");
			getLogger().warning("========================================================");

			File configFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml");
			File oldConfigFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.old.yml");
			if (oldConfigFile.getAbsoluteFile().exists()) {
				oldConfigFile.getAbsoluteFile().delete();
			}
			configFile.getAbsoluteFile().renameTo(oldConfigFile.getAbsoluteFile());
			saveDefaultConfig();
			try {
				getConfig().load(configFile.getAbsoluteFile());
			} catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
				getLogger().warning("Could not load freshly generated config file!");
				e.printStackTrace();
			}
		} else if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
			getLogger().warning("========================================================");
			getLogger().warning("YOU ARE USING AN OLD CONFIG FILE!");
			getLogger().warning("This is not a problem, as LumberJack will just use the");
			getLogger().warning("default settings for unset values. However, if you want");
			getLogger().warning("to configure the new options, please go to");
			getLogger().warning("https://www.spigotmc.org/resources/1-13-lumberjack.60306/");
			getLogger().warning("and replace your config.yml with the new one. You can");
			getLogger().warning("then insert your old changes into the new file.");
			getLogger().warning("========================================================");
			usingMatchingConfig = false;
		}
		
		File playerDataFolder = new File(getDataFolder().getPath() + File.separator + "playerdata");
		if (!playerDataFolder.getAbsoluteFile().exists()) {
			playerDataFolder.mkdir();
		}
		
		getConfig().addDefault("gravity-enabled-by-default", false);
		getConfig().addDefault("check-for-updates", "true");
		getConfig().addDefault("show-message-again-after-logout", true);
		getConfig().addDefault("attached-logs-fall-down", true);
		
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

	public boolean isPartOfTree(Block block) {
		
		for(String blockName : treeBlockNames) {
			if(Material.matchMaterial(blockName) != null) {
				if(Material.matchMaterial(blockName) == block.getType()) {
					getLogger().warning(block.getType() + " IS TREE");
					return true;
				}
			} else {
				getLogger().warning("Block type not found: " + blockName);
			}
		}
		
		return false;
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

//	public boolean isLog(Block block) {
//		Material type = block.getType();
//		for (String woodType : woodTypes) {
//			if (type.name().equalsIgnoreCase(woodType + "_log")) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	public boolean isLeaves(Block block) {
//		Material type = block.getType();
//		for (String woodType : woodTypes) {
//			if (type.name().equalsIgnoreCase(woodType + "_leaves")) {
//				return true;
//			}
//		}
//		return false;
//	}

	public Block[] getLogsAbove(Block block) {
		ArrayList<Block> list = new ArrayList<Block>();
		Block currentBlock = block.getRelative(BlockFace.UP);
		while (isPartOfTree(currentBlock) && list.size() < maxTreeSize) {
			list.add(currentBlock);
			currentBlock = currentBlock.getRelative(BlockFace.UP);
		}
		return list.toArray(new Block[list.size()]);
	}
	
//	public boolean isTreePart(Block block) {
//		return (isLog(block) || isLeaves(block));
//	}
	
	public void getTreeTrunk(Block block,ArrayList<Block> list) {
		BlockFace[] faces = {
				BlockFace.UP,
				BlockFace.EAST,BlockFace.WEST,BlockFace.NORTH,BlockFace.SOUTH,
				BlockFace.SOUTH_WEST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.NORTH_EAST
		};
		
		Block currentBlock = block;
		
		if(isPartOfTree(currentBlock) && list.size()<maxTreeSize) {
			if(!list.contains(currentBlock)) {
				list.add(currentBlock);
				
				for(BlockFace face:faces) {
					getTreeTrunk(currentBlock.getRelative(face),list);
				}
				
			}
			
		}
	}
	

}