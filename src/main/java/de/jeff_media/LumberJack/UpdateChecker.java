package de.jeff_media.LumberJack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UpdateChecker {

	private LumberJack plugin;

	public UpdateChecker(LumberJack plugin) {
		this.plugin = plugin;
	}

	String latestVersionLink = "https://api.jeff-media.de/lumberjack/lumberjack-latest-version.txt";
	String downloadLink = "https://www.spigotmc.org/resources/1-13-lumberjack.60306/";
	private String currentVersion = "undefined";
	private String latestVersion = "undefined";

	public void sendUpdateMessage(Player p) {
		if(!latestVersion.equals("undefined")) {
		if (!currentVersion.equals(latestVersion)) {
			p.sendMessage(ChatColor.GRAY + "There is a new version of " + ChatColor.GOLD + "LumberJack" + ChatColor.GRAY
					+ " available.");
					p.sendMessage(ChatColor.GRAY + "Please download at " + downloadLink);
		}
		}
	}

	public void checkForUpdate() {
		
		plugin.getLogger().info("Checking for available updates...");

		try {

			HttpURLConnection httpcon = (HttpURLConnection) new URL(latestVersionLink).openConnection();
			httpcon.addRequestProperty("User-Agent", "LumberJack/"+plugin.getDescription().getVersion());

			BufferedReader reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

			String inputLine = reader.readLine().trim();

			latestVersion = inputLine;
			currentVersion = plugin.getDescription().getVersion().trim();

			
			if (latestVersion.equals(currentVersion)) {
				plugin.getLogger().info("You are using the latest version of LumberJack.");
			} else {
				plugin.getLogger().warning("========================================================");
				plugin.getLogger().warning("There is a new version of LumberJack available!");
				plugin.getLogger().warning("Latest : " + inputLine);
				plugin.getLogger().warning("Current: " + currentVersion);
				plugin.getLogger().warning("Please update to the newest version. Download:");
				plugin.getLogger().warning(downloadLink);
				plugin.getLogger().warning("========================================================");
			}

			reader.close();
		} catch (Exception e) {
			plugin.getLogger().warning("Could not check for updates.");
		}

	}

}