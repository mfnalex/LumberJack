package de.jeffclan.LumberJack;

public class PlayerSetting {
	// Sorting enabled for this player?
	boolean gravityEnabled;

	// Did we already show the message how to activate sorting?
	boolean hasSeenMessage = false;

	PlayerSetting(boolean gravityEnabled) {
		this.gravityEnabled = gravityEnabled;
	}
}