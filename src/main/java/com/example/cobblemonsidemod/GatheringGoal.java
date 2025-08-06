package com.example.cobblemonsidemod;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GatheringGoal extends Goal {
    private final PokemonEntity pokemonEntity;
    private final Random random;
    private BlockPos targetBlock;

    public GatheringGoal(PokemonEntity pokemonEntity) {
        this.pokemonEntity = pokemonEntity;
        this.random = new Random();
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.pokemonEntity.getPokemon().getSpecies().getName().equalsIgnoreCase("teddiursa")) {
            return false;
        }
        if (pokemonEntity.getNavigation().isIdle() && pokemonEntity.getWorld() != null) {
            List<BlockPos> validBlocks = StreamSupport.stream(BlockPos.iterate(
                    pokemonEntity.getBlockPos().add(-10, -5, -10),
                    pokemonEntity.getBlockPos().add(10, 5, 10)).spliterator(), false)
                .filter(pos -> isBerryOrCrop(pokemonEntity.getWorld().getBlockState(pos)))
                .map(BlockPos::toImmutable)
                .collect(Collectors.toList());

            if (!validBlocks.isEmpty()) {
                targetBlock = validBlocks.get(random.nextInt(validBlocks.size()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return targetBlock != null && !pokemonEntity.getNavigation().isIdle() && pokemonEntity.getBlockPos().getSquaredDistance(targetBlock) > 2.0;
    }

    @Override
    public void start() {
        if (targetBlock != null) {
            pokemonEntity.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
        }
    }

    @Override
    public void tick() {
        if (targetBlock != null && pokemonEntity.getBlockPos().getSquaredDistance(targetBlock) < 4.0) {
            World world = pokemonEntity.getWorld();
            BlockState state = world.getBlockState(targetBlock);
            if (isBerryOrCrop(state)) {
                world.spawnEntity(new ItemEntity(world, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), new ItemStack(Items.SWEET_BERRIES)));
                if (state.getBlock() instanceof CropBlock) {
                    world.setBlockState(targetBlock, ((CropBlock) state.getBlock()).getDefaultState());
                }
            }
            targetBlock = null;
        }
    }

    private boolean isBerryOrCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock) {
            return ((CropBlock) block).isMature(state);
        }
        return false;
    }
}
