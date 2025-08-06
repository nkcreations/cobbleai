// Archivo: CuriousGoal.java
// Implementa el comportamiento curioso de seguir al jugador.
package com.example.cobblemonsidemod;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import java.util.EnumSet;

public class CuriousGoal extends Goal {
    private final MobEntity mob;
    private final double speed;
    private final float maxDistance;
    private PlayerEntity targetPlayer;

    public CuriousGoal(MobEntity mob, double speed, float maxDistance) {
        this.mob = mob;
        this.speed = speed;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.targetPlayer = this.mob.getWorld().getClosestPlayer(this.mob, this.maxDistance);
        if (this.targetPlayer == null) {
            return false;
        }
        return this.mob.squaredDistanceTo(this.targetPlayer) <= (this.maxDistance * this.maxDistance);
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        if (this.mob.squaredDistanceTo(this.targetPlayer) > (this.maxDistance * this.maxDistance)) {
            return false;
        }
        return !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(this.targetPlayer, this.speed);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.targetPlayer = null;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.targetPlayer, 10.0F, (float) this.mob.getMaxLookPitchChange());
    }
}
