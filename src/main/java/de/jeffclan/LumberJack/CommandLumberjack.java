package de.jeffclan.LumberJack;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLumberjack implements CommandExecutor {
	
	LumberJack plugin;
	
	public CommandLumberjack(LumberJack plugin) {
		this.plugin=plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		
		
		
		if(!command.getName().equalsIgnoreCase("lumberjack")) {
			return false;
		}
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command.");
			return true;
		}
		Player p = (Player) sender;
		if(!sender.hasPermission("lumberjack.use")) {
			sender.sendMessage(plugin.getCommand("lumberjack").getPermissionMessage());
			return true;
		}
		
		if(sender.hasPermission("lumberjack.force") && !sender.hasPermission("lumberjack.force.ignore")) {
			sender.sendMessage(plugin.messages.MSG_CAN_NOT_DISABLE);
			return true;
		}
		
		plugin.togglePlayerSetting(p);
		if(plugin.getPlayerSetting(p).gravityEnabled) {
			sender.sendMessage(plugin.messages.MSG_ACTIVATED);
		} else {
			sender.sendMessage(plugin.messages.MSG_DEACTIVATED);
		}
		return true;
		
	}

}