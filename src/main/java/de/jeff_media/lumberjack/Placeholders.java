package de.jeff_media.lumberjack;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "lumberjack";
    }

    @Override
    public @NotNull String getAuthor() {
        return null;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(!player.isOnline()) return "unknown";
        if(params.equals("enabled")) {
            return String.valueOf(LumberJack.getInstance().getPlayerSetting(player.getPlayer()).gravityEnabled);
        }
        return "";
    }

    @Override
    public @NotNull String getVersion() {
        return LumberJack.getInstance().getDescription().getVersion();
    }
}
