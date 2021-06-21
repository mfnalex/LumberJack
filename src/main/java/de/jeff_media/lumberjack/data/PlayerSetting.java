package de.jeff_media.lumberjack.data;

public class PlayerSetting {
    // Sorting enabled for this player?
    public boolean gravityEnabled;

    // Did we already show the message how to activate sorting?
    public boolean hasSeenMessage = false;

    public PlayerSetting(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }
}