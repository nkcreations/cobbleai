package com.example.cobblemonsidemod;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Random;

public class FlyingPathfindGoal extends Goal {
    private final PokemonEntity pokemonEntity;
    private final Random random;
    private final int minHeight;
    private final int maxHeight;
    private Path path;
    private BlockPos target;

    public FlyingPathfindGoal(PokemonEntity pokemonEntity, int minHeight, int maxHeight) {
        this.pokemonEntity = pokemonEntity;
        this.random = new Random();
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Goal can only start if the Pokémon is a Swablu
        if (!this.pokemonEntity.getPokemon().getSpecies().getName().equalsIgnoreCase("swablu")) {
            return false;
        }
        return pokemonEntity.getNavigation().isIdle() && pokemonEntity.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return !pokemonEntity.getNavigation().isIdle();
    }

    @Override
    public void start() {
        Vec3d targetVec = findTargetPosition();
        if (targetVec != null) {
            this.target = BlockPos.ofFloored(targetVec);
            this.path = pokemonEntity.getNavigation().findPathTo(this.target, 32);
            if (this.path != null) {
                pokemonEntity.getNavigation().startMovingAlong(this.path, 1.0);
            }
        }
    }

    @Override
    public void stop() {
        this.pokemonEntity.getNavigation().stop();
        this.path = null;
    }

    private Vec3d findTargetPosition() {
        for (int i = 0; i < 10; ++i) {
            Vec3d currentPos = pokemonEntity.getPos();
            Vec3d randomPos = new Vec3d(
                currentPos.getX() + random.nextInt(21) - 10,
                currentPos.getY() + random.nextInt(maxHeight - minHeight + 1) + minHeight,
                currentPos.getZ() + random.nextInt(21) - 10
            );

            if (pokemonEntity.getWorld().isAir(BlockPos.ofFloored(randomPos))) {
                return randomPos;
            }
        }
        return null;
    }
}
