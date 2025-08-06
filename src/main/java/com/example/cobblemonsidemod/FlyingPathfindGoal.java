// Archivo: FlyingPathfindGoal.java
// Implementa el comportamiento de vuelo personalizado.
package com.example.cobblemonsidemod;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;
import java.util.Random;

public class FlyingPathfindGoal extends Goal {
    private final MobEntity mob;
    private final Random random;
    private final int minHeight;
    private final int maxHeight;
    private Path path;
    private BlockPos target;

    public FlyingPathfindGoal(MobEntity mob, int minHeight, int maxHeight) {
        this.mob = mob;
        this.random = new Random();
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // La meta puede comenzar si el Pokémon está volando y no tiene una ruta o si la ruta ha terminado.
        return mob.getNavigation().isIdle() && mob.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        // La meta continúa si la ruta no ha terminado.
        return !mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        // Encuentra una nueva posición de vuelo.
        Vec3d targetVec = findTargetPosition();
        if (targetVec != null) {
            this.target = BlockPos.ofFloored(targetVec);
            this.path = mob.getNavigation().findPathTo(this.target, 32);
            if (this.path != null) {
                mob.getNavigation().startMovingAlong(this.path, 1.0); // 1.0 es la velocidad
            }
        }
    }

    @Override
    public void stop() {
        // Detiene la navegación cuando la meta termina.
        this.mob.getNavigation().stop();
        this.path = null;
    }

    private Vec3d findTargetPosition() {
        // Busca una posición aleatoria para volar.
        for (int i = 0; i < 10; ++i) {
            Vec3d currentPos = mob.getPos();
            Vec3d randomPos = new Vec3d(
                currentPos.getX() + random.nextInt(21) - 10,
                currentPos.getY() + random.nextInt(maxHeight - minHeight + 1) + minHeight,
                currentPos.getZ() + random.nextInt(21) - 10
            );

            if (mob.getWorld().isAir(BlockPos.ofFloored(randomPos))) {
                 return randomPos;
            }
        }
        return null;
    }
}
