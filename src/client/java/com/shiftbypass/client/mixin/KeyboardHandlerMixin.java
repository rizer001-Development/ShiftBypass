package com.shiftbypass.client.mixin;

import com.shiftbypass.client.ShiftBypassState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla rule being bypassed: while any {@link net.minecraft.client.gui.screens.Screen} is
 * open, {@code KeyboardHandler.keyPress} only ever forwards key events to the screen
 * (chat, inventory, crafting table, ...). Game key mappings are never pressed - the
 * only shift handling left is releasing the mapping when the screen swallows the key.
 *
 * <p>With the bypass enabled, a shift press is instead routed straight into the vanilla
 * sneak key mapping via {@link KeyMapping#setDown}. That call goes through the mapping's
 * normal logic, so the vanilla sneak settings behave exactly as configured:
 * <ul>
 *   <li>hold mode - the sneak mapping is pressed (and stays pressed, re-pressed on key
 *       repeats, so you keep sneaking if a screen opens while you hold shift)</li>
 *   <li>toggle mode - {@code setDown(true)} toggles the mapping, matching the vanilla
 *       toggle behavior for open screens</li>
 * </ul>
 * Shift releases are left to vanilla: it already releases the mapping on any key
 * release, screen or no screen, and touching toggle mode here would double-toggle.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

	@Inject(
			method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void shiftbypass$pressShiftWhileScreenOpen(long windowPointer, int action, KeyEvent keyEvent, CallbackInfo ci) {
		if (!ShiftBypassState.isEnabled()) {
			return;
		}
		// Press events only: releases are handled fine by vanilla.
		if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) {
			return;
		}
		if (keyEvent.key() != GLFW.GLFW_KEY_LEFT_SHIFT && keyEvent.key() != GLFW.GLFW_KEY_RIGHT_SHIFT) {
			return;
		}
		// No screen open - vanilla handles the key exactly as configured, nothing to bypass.
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.gui.screen() == null || minecraft.player == null) {
			return;
		}

		KeyMapping shift = minecraft.options.keyShift;
		// Ignore rebinds to mouse buttons or unknown keys: only real keyboard keys
		// can be driven from a keyboard event.
		if (shift.isUnbound()
				|| ((KeyMappingAccessor) (Object) shift).shiftbypass$key().getType() != com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM) {
			return;
		}

		// GLFW_REPEAT only matters in hold mode; in toggle mode setDown(true) would
		// toggle the mapping a second time, so repeats are dropped there.
		boolean holdMode = !shift.isDown() || action == GLFW.GLFW_PRESS;
		if (holdMode) {
			shift.setDown(true);
		}
		ci.cancel();
	}
}
