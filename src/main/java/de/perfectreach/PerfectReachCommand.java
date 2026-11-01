package de.perfectreach;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PerfectReachCommand {
    private PerfectReachCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("perfectreach")
                        .then(literal("toggle").executes(ctx -> {
                            PerfectReachConfig c = PerfectReachConfig.get();
                            c.enabled = !c.enabled;
                            PerfectReachConfig.save();
                            return msg(ctx, "Perfect Reach Sound: " + (c.enabled ? "AN" : "AUS"));
                        }))
                        .then(literal("volume")
                                .then(argument("value", FloatArgumentType.floatArg(0f, 1f)).executes(ctx -> {
                                    PerfectReachConfig.get().volume = FloatArgumentType.getFloat(ctx, "value");
                                    PerfectReachConfig.save();
                                    return msg(ctx, "Lautstärke: " + PerfectReachConfig.get().volume);
                                })))
                        .then(literal("pitch")
                                .then(argument("value", FloatArgumentType.floatArg(0.5f, 2f)).executes(ctx -> {
                                    PerfectReachConfig.get().pitch = FloatArgumentType.getFloat(ctx, "value");
                                    PerfectReachConfig.save();
                                    return msg(ctx, "Tonhöhe: " + PerfectReachConfig.get().pitch);
                                })))
                        .then(literal("interval")
                                .then(argument("ticks", IntegerArgumentType.integer(1, 40)).executes(ctx -> {
                                    PerfectReachConfig.get().intervalTicks = IntegerArgumentType.getInteger(ctx, "ticks");
                                    PerfectReachConfig.save();
                                    return msg(ctx, "Intervall: " + PerfectReachConfig.get().intervalTicks + " Ticks");
                                })))
                        .then(literal("window")
                                .then(argument("size", DoubleArgumentType.doubleArg(0.05, 2.0)).executes(ctx -> {
                                    PerfectReachConfig c = PerfectReachConfig.get();
                                    c.windowSize = DoubleArgumentType.getDouble(ctx, "size");
                                    c.useAttributeRange = true;
                                    PerfectReachConfig.save();
                                    return msg(ctx, "Modus: Attribut-Reichweite, Fenster = " + c.windowSize + " Blöcke");
                                })))
                        .then(literal("range")
                                .then(argument("min", DoubleArgumentType.doubleArg(0.0, 8.0))
                                        .then(argument("max", DoubleArgumentType.doubleArg(0.0, 8.0)).executes(ctx -> {
                                            PerfectReachConfig c = PerfectReachConfig.get();
                                            c.minDistance = DoubleArgumentType.getDouble(ctx, "min");
                                            c.maxDistance = DoubleArgumentType.getDouble(ctx, "max");
                                            c.useAttributeRange = false;
                                            PerfectReachConfig.save();
                                            return msg(ctx, "Modus: feste Werte " + c.minDistance + " - " + c.maxDistance);
                                        }))))
                        .then(literal("status").executes(ctx -> {
                            PerfectReachConfig c = PerfectReachConfig.get();
                            String range = c.useAttributeRange
                                    ? "Attribut-Reichweite, Fenster " + c.windowSize
                                    : "fest " + c.minDistance + " - " + c.maxDistance;
                            return msg(ctx, (c.enabled ? "AN" : "AUS") + " | " + range
                                    + " | Vol " + c.volume + " | Pitch " + c.pitch
                                    + " | Intervall " + c.intervalTicks);
                        }))
                ));
    }

    private static int msg(CommandContext<FabricClientCommandSource> ctx, String text) {
        ctx.getSource().sendFeedback(Text.literal(text));
        return Command.SINGLE_SUCCESS;
    }
}
