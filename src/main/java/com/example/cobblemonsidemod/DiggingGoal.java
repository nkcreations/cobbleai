// Archivo: DiggingGoal.java
// Implementa el comportamiento de topos que cavan túneles.
package com.example.cobblemonsidemod;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class DiggingGoal extends Goal {
    private final MobEntity mob;
    private final Random random;
    private final List<String> diggableBlocks;
    private final int blockBreakTime;
    private BlockPos targetBlock;
    private int diggingTimer;

    public DiggingGoal(MobEntity mob, String[] diggableBlocks, int blockBreakTime) {
        this.mob = mob;
        this.random = new Random();
        this.diggableBlocks = List.of(diggableBlocks);
        this.blockBreakTime = blockBreakTime;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (mob.getNavigation().isIdle() && mob.getWorld() != null) {
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
            mob.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
        }
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;

        // Simula la excavación cuando está cerca del bloque.
        if (mob.getBlockPos().getSquaredDistance(targetBlock) < 4.0) {
            diggingTimer++;
            mob.getWorld().setBlockBreakingInfo(mob.getId(), targetBlock, (int)((float)diggingTimer / blockBreakTime * 10.0F));

            if (diggingTimer >= blockBreakTime) {
                // Rompe el bloque.
                mob.getWorld().breakBlock(targetBlock, true);

                // Encuentra el siguiente bloque para continuar el túnel.
                targetBlock = findNextTunnelBlock();
                if (targetBlock != null) {
                    diggingTimer = 0;
                    mob.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
                } else {
                    targetBlock = null;
                }
            }
        }
    }

    @Override
    public void stop() {
        if (targetBlock != null) {
            mob.getWorld().setBlockBreakingInfo(mob.getId(), targetBlock, -1);
        }
        targetBlock = null;
        diggingTimer = 0;
        mob.getNavigation().stop();
    }

    private BlockPos findDiggableBlock() {
        // Busca un bloque inicial en un radio de 5 bloques.
        for (int i = 0; i < 10; i++) {
            BlockPos pos = mob.getBlockPos().add(random.nextInt(11) - 5, random.nextInt(5) - 2, random.nextInt(11) - 5);
            if (isDiggable(pos)) {
                return pos;
            }
        }
        return null;
    }

    private BlockPos findNextTunnelBlock() {
        // Crea un túnel de un bloque de ancho en una dirección horizontal.
        if (targetBlock == null) return null;
        Direction direction = Direction.fromHorizontal(random.nextInt(4));
        BlockPos nextPos = targetBlock.offset(direction);
        if (isDiggable(nextPos)) {
            return nextPos;
        }
        return null;
    }

    private boolean isDiggable(BlockPos pos) {
        // Comprueba si el bloque es de un tipo que el Pokémon puede excavar.
        BlockState state = mob.getWorld().getBlockState(pos);
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        return diggableBlocks.contains(blockId.toString());
    }
}
