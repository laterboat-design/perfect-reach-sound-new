package de.perfectreach;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class PerfectReachClient implements ClientModInitializer {
    public static final String MOD_ID = "perfectreachsound";

    private int cooldown = 0;

    @Override
    public void onInitializeClient() {
        PerfectReachConfig.load();
        PerfectReachCommand.register();
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (cooldown > 0) cooldown--;

        PerfectReachConfig cfg = PerfectReachConfig.get();
        if (!cfg.enabled
                || mc.player == null
                || mc.world == null
                || mc.currentScreen != null
                || mc.player.isSpectator()) {
            return;
        }

        double min;
        double max;
        if (cfg.useAttributeRange) {
            max = mc.player.getEntityInteractionRange();
            min = max - cfg.windowSize;
        } else {
            min = cfg.minDistance;
            max = cfg.maxDistance;
        }

        double dist = ReachChecker.distanceToTarget(mc, max, cfg);
        if (dist < 0 || dist < min || dist > max) {
            return;
        }

        if (cfg.requireAttackReady && mc.player.getAttackCooldownProgress(0.5f) < 0.9f) {
            return;
        }

        if (cooldown == 0) {
            mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), cfg.pitch, cfg.volume));
            cooldown = cfg.intervalTicks;
        }
    }
}
