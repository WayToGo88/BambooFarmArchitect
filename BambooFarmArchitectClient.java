package com.example.bamboofarmarchitect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

/**
 * Client-side planning assistant. It does not automatically break/place blocks
 * or bypass server protections. It computes 3x3 drill coverage and reports
 * common falling-block hazards before the user runs their drill.
 */
public class BambooFarmArchitectClient implements ClientModInitializer {
    private static BlockPos start = null;
    private static int width = 32, length = 32, depth = 8;
    private static int cursor = 0;
    private static boolean paused = true;
    private static boolean showHelp = true;
    private static final List<String> FALLING_OR_FLUID_HAZARDS = List.of(
            "sand", "red_sand", "gravel", "anvil", "chipped_anvil",
            "damaged_anvil", "water", "lava", "powder_snow"
    );

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("bfa")
                .then(literal("help").executes(ctx -> {
                    help(ctx.getSource());
                    return 1;
                }))
                .then(literal("start").executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    start = player.blockPosition();
                    cursor = 0;
                    paused = true;
                    ctx.getSource().sendFeedback(Component.literal(
                        "BFA: Start point set to " + start.toShortString()
                        + ". Configure dimensions with /bfa size <width> <length> <depth>, then /bfa plan."
                    ));
                    return 1;
                }))
                .then(literal("size")
                    .then(argument("width", integer(3, 512))
                    .then(argument("length", integer(3, 512))
                    .then(argument("depth", integer(1, 128)).executes(ctx -> {
                        width = ctx.getArgument("width", Integer.class);
                        length = ctx.getArgument("length", Integer.class);
                        depth = ctx.getArgument("depth", Integer.class);
                        cursor = 0;
                        ctx.getSource().sendFeedback(Component.literal(
                            "BFA: Area set to " + width + " x " + length + " x " + depth
                            + " (" + ((long) width * length * depth) + " blocks)."
                        ));
                        return 1;
                    })))))
                .then(literal("plan").executes(ctx -> {
                    if (start == null) {
                        ctx.getSource().sendFeedback(Component.literal("BFA: Set the start point first with /bfa start."));
                        return 0;
                    }
                    long blocks = (long) width * length * depth;
                    long passes = ((width + 2L) / 3L) * ((length + 2L) / 3L) * depth;
                    ctx.getSource().sendFeedback(Component.literal(
                        "BFA excavation plan: " + width + "x" + length + "x" + depth
                        + " = " + blocks + " blocks; about " + passes
                        + " 3x3 drill positions. Starting corner: " + start.toShortString() + "."
                    ));
                    ctx.getSource().sendFeedback(Component.literal(
                        "Hazards to check before drilling: sand, red sand, gravel, anvils, water, lava, powder snow, caves, and unsupported ceilings. This client assistant cannot reliably detect every hazard or control a server's drill."
                    ));
                    return 1;
                }))
                .then(literal("pause").executes(ctx -> {
                    paused = true;
                    ctx.getSource().sendFeedback(Component.literal("BFA: Paused."));
                    return 1;
                }))
                .then(literal("resume").executes(ctx -> {
                    paused = false;
                    ctx.getSource().sendFeedback(Component.literal(
                        "BFA: Planning resumed. This does not activate the drill; use your drill manually and advance checkpoints with /bfa next."
                    ));
                    return 1;
                }))
                .then(literal("next").executes(ctx -> {
                    if (start == null) {
                        ctx.getSource().sendFeedback(Component.literal("BFA: Set the start point first with /bfa start."));
                        return 0;
                    }
                    long total = ((width + 2L) / 3L) * ((length + 2L) / 3L) * depth;
                    if (cursor < total) cursor++;
                    ctx.getSource().sendFeedback(Component.literal("BFA checkpoint: " + cursor + " / " + total
                        + " drill positions. Falling-block/fluid checks are manual."));
                    return 1;
                }))
                .then(literal("status").executes(ctx -> {
                    ctx.getSource().sendFeedback(Component.literal(
                        "BFA: " + (start == null ? "start not set" : "start=" + start.toShortString())
                        + ", area=" + width + "x" + length + "x" + depth
                        + ", checkpoint=" + cursor + ", state=" + (paused ? "PAUSED" : "READY")
                    ));
                    return 1;
                }))
            );
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Intentionally no automated block breaking/placement.
        });
    }

    private static void help(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("Bamboo Farm Architect commands:"));
        source.sendFeedback(Component.literal("/bfa start — mark current block as excavation start"));
        source.sendFeedback(Component.literal("/bfa size <width 3-512> <length 3-512> <depth 1-128>"));
        source.sendFeedback(Component.literal("/bfa plan — calculate block count and approximate 3x3 passes"));
        source.sendFeedback(Component.literal("/bfa next — record a completed drill position"));
        source.sendFeedback(Component.literal("/bfa pause | /bfa resume | /bfa status"));
        source.sendFeedback(Component.literal("Sand/gravel safety: check overhead and adjacent blocks before each pass; this version does not auto-detect or remove hazards."));
    }
}
