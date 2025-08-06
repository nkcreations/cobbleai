package com.example.cobblemonsidemod;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class DiggingGoal extends Goal {
    private final PokemonEntity pokemonEntity;
    private final Random random;
    private final List<String> diggableBlocks;
    private final int blockBreakTime;
    private BlockPos targetBlock;
    private int diggingTimer;

    public DiggingGoal(PokemonEntity pokemonEntity, String[] diggableBlocks, int blockBreakTime) {
        this.pokemonEntity = pokemonEntity;
        this.random = new Random();
        this.diggableBlocks = List.of(diggableBlocks);
        this.blockBreakTime = blockBreakTime;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.pokemonEntity.getPokemon().getSpecies().getName().equalsIgnoreCase("diglett")) {
            return false;
        }
        if (pokemonEntity.getNavigation().isIdle() && pokemonEntity.getWorld() != null) {
            targetBlock = findDiggableBlock();
            return targetBlock != null;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return targetBlock != null && diggingTimer < blockBreakTime;
    }

    @Override
    public void start() {
        diggingTimer = 0;
        if (targetBlock != null) {
            pokemonEntity.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
        }
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;

        if (pokemonEntity.getBlockPos().getSquaredDistance(targetBlock) < 4.0) {
            diggingTimer++;
            pokemonEntity.getWorld().setBlockBreakingInfo(pokemonEntity.getId(), targetBlock, (int) ((float) diggingTimer / blockBreakTime * 10.0F));

            if (diggingTimer >= blockBreakTime) {
                pokemonEntity.getWorld().breakBlock(targetBlock, true);
                targetBlock = findNextTunnelBlock();
                if (targetBlock != null) {
                    diggingTimer = 0;
                    pokemonEntity.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
                }
            }
        }
    }

    @Override
    public void stop() {
        if (targetBlock != null) {
            pokemonEntity.getWorld().setBlockBreakingInfo(pokemonEntity.getId(), targetBlock, -1);
        }
        targetBlock = null;
        diggingTimer = 0;
        pokemonEntity.getNavigation().stop();
    }

    private BlockPos findDiggableBlock() {
        for (int i = 0; i < 10; i++) {
            BlockPos pos = pokemonEntity.getBlockPos().add(random.nextInt(11) - 5, random.nextInt(5) - 2, random.nextInt(11) - 5);
            if (isDiggable(pos)) {
                return pos;
            }
        }
        return null;
    }

    private BlockPos findNextTunnelBlock() {
        if (targetBlock == null) return null;
        Direction direction = Direction.fromHorizontal(random.nextInt(4));
        BlockPos nextPos = targetBlock.offset(direction);
        if (isDiggable(nextPos)) {
            return nextPos;
        }
        return null;
    }

    private boolean isDiggable(BlockPos pos) {
        BlockState state = pokemonEntity.getWorld().getBlockState(pos);
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        return diggableBlocks.contains(blockId.toString());
    }
}
