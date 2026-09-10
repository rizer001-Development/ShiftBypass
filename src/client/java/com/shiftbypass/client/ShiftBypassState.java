package com.shiftbypass.client;

/**
 * Global toggle for the mod, flipped by the /sb command.
 *
 * <p>Deliberately not persisted: vanilla never lets you sneak while a screen is
 * open, so silently restoring that behavior after every launch is the safest
 * default. Use /sb on after joining a world if you want it active.
 */
public final class ShiftBypassState {
	private static volatile boolean enabled = false;

	private ShiftBypassState() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}
}
