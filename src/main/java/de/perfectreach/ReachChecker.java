package de.perfectreach;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class ReachChecker {
    private ReachChecker() {}

    /**
     * Liefert die Distanz (Auge -> getroffener Punkt der Hitbox) zum Lebewesen im Fadenkreuz,
     * oder -1, wenn kein gültiges Ziel anvisiert wird.
     */
    public static double distanceToTarget(MinecraftClient mc, double range, PerfectReachConfig cfg) {
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if (player == null || world == null) return -1;

        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        double searchLength = range + 1.0;
        Vec3d end = eye.add(look.multiply(searchLength));

        BlockHitResult blockHit = world.raycast(new RaycastContext(
                eye, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player));
        double blockDistSq = blockHit.getType() == HitResult.Type.MISS
                ? Double.MAX_VALUE
                : eye.squaredDistanceTo(blockHit.getPos());

        Box searchBox = player.getBoundingBox()
                .stretch(look.multiply(searchLength))
                .expand(1.0, 1.0, 1.0);

        EntityHitResult hit = ProjectileUtil.raycast(
                player, eye, end, searchBox,
                e -> !e.isSpectator() && e.canHit(),
                searchLength * searchLength);

        if (hit == null || !(hit.getEntity() instanceof LivingEntity target) || !target.isAlive()) {
            return -1;
        }

        boolean isPlayer = target instanceof PlayerEntity;
        if (isPlayer && !cfg.includePlayers) return -1;
        if (!isPlayer && !cfg.includeMobs) return -1;

        double distSq = eye.squaredDistanceTo(hit.getPos());
        if (distSq > blockDistSq) return -1;

        return Math.sqrt(distSq);
    }
}
