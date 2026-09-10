package com.shiftbypass.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ShiftBypassClient implements ClientModInitializer {
	public static final String MOD_ID = "shiftbypass";
	public static final Logger LOGGER = LoggerFactory.getLogger("ShiftBypass");

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			var sb = literal("sb")
					.executes(ctx -> feedback(ctx.getSource(),
							"ShiftBypass is " + (ShiftBypassState.isEnabled() ? "ON" : "OFF")
									+ " - use /sb <on|off>"))
					.then(literal("on").executes(ctx -> {
						ShiftBypassState.setEnabled(true);
						return feedback(ctx.getSource(), "ShiftBypass: ON - shift now works while any screen is open");
					}))
					.then(literal("off").executes(ctx -> {
						ShiftBypassState.setEnabled(false);
						return feedback(ctx.getSource(), "ShiftBypass: OFF");
					}));

			dispatcher.register(sb);
			// Alias so /shiftbypass works too.
			dispatcher.register(literal("shiftbypass").redirect(sb.build()));
		});
	}

	private static int feedback(FabricClientCommandSource source, String message) {
		source.sendFeedback(Component.literal(message));
		return 1;
	}
}
