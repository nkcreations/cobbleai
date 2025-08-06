package com.example.cobblemonsidemod;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class CuriousGoal extends Goal {
    private final PokemonEntity pokemonEntity;
    private final double speed;
    private final float maxDistance;
    private PlayerEntity targetPlayer;

    public CuriousGoal(PokemonEntity pokemonEntity, double speed, float maxDistance) {
        this.pokemonEntity = pokemonEntity;
        this.speed = speed;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.pokemonEntity.getPokemon().getSpecies().getName().equalsIgnoreCase("spheal")) {
            return false;
        }
        this.targetPlayer = this.pokemonEntity.getWorld().getClosestPlayer(this.pokemonEntity, this.maxDistance);
        if (this.targetPlayer == null) {
            return false;
        }
        return this.pokemonEntity.squaredDistanceTo(this.targetPlayer) <= (this.maxDistance * this.maxDistance);
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        if (this.pokemonEntity.squaredDistanceTo(this.targetPlayer) > (this.maxDistance * this.maxDistance)) {
            return false;
        }
        return !this.pokemonEntity.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.pokemonEntity.getNavigation().startMovingTo(this.targetPlayer, this.speed);
    }

    @Override
    public void stop() {
        this.pokemonEntity.getNavigation().stop();
        this.targetPlayer = null;
    }

    @Override
    public void tick() {
        this.pokemonEntity.getLookControl().lookAt(this.targetPlayer, 10.0F, (float) this.pokemonEntity.getMaxLookPitchChange());
    }
}
