package de.jeff_media.lumberjack.config;


import de.jeff_media.lumberjack.LumberJack;
import org.bukkit.ChatColor;

public class Messages {


    public final String MSG_ACTIVATED;
    public final String MSG_DEACTIVATED;
    public final String MSG_COMMANDMESSAGE;
    public final String MSG_COMMANDMESSAGE2;
    public final String MSG_CAN_NOT_DISABLE;
    final LumberJack plugin;

    public Messages(LumberJack plugin) {
        this.plugin = plugin;

        MSG_ACTIVATED = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                .getString("message-gravity-enabled", "&7Tree gravity has been &aenabled&7.&r"));

        MSG_DEACTIVATED = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                .getString("message-gravity-disabled", "&7Tree gravity has been &cdisabled&7.&r"));

        MSG_COMMANDMESSAGE = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
                "message-when-breaking-log", "&7Hint: Type &6/lumberjack&7 to enable tree gravity."));

        MSG_COMMANDMESSAGE2 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
                "message-when-breaking-log2", "&7Hint: Type &6/lumberjack&7 to disable tree gravity."));

        MSG_CAN_NOT_DISABLE = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
                "message-can-not-disable", "&cYou are not allowed to disable tree gravity."));


    }

}