package de.jeff_media.lumberjack.commands;

import de.jeff_media.jefflib.BlockTracker;
import de.jeff_media.lumberjack.LumberJack;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandLumberjack implements CommandExecutor {

    final LumberJack plugin;

    public CommandLumberjack(LumberJack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {


        if (!command.getName().equalsIgnoreCase("lumberjack")) {
            return false;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("lumberjack.reload")) {
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "LumberJack has been reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("debug") && sender.hasPermission("lumberjack.debug")) {
            Player player = (Player) sender;
            Block target = player.getTargetBlock(null, 20);
            player.sendMessage(String.valueOf(BlockTracker.isPlayerPlacedBlock(target)));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to run this command.");
            return true;
        }
        Player p = (Player) sender;
        if (!sender.hasPermission("lumberjack.use")) {
            sender.sendMessage(Objects.requireNonNull(Objects.requireNonNull(plugin.getCommand("lumberjack")).getPermissionMessage()));
            return true;
        }

        if (sender.hasPermission("lumberjack.force") && !sender.hasPermission("lumberjack.force.ignore")) {
            sender.sendMessage(plugin.messages.MSG_CAN_NOT_DISABLE);
            return true;
        }

        plugin.togglePlayerSetting(p);
        if (plugin.getPlayerSetting(p).gravityEnabled) {
            sender.sendMessage(plugin.messages.MSG_ACTIVATED);
        } else {
            sender.sendMessage(plugin.messages.MSG_DEACTIVATED);
        }
        return true;

    }

}