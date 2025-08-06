// Archivo: GatheringGoal.java
// Implementa el comportamiento de recolección de bayas/cultivos.
package com.example.cobblemonsidemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
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
    private final MobEntity mob;
    private final Random random;
    private BlockPos targetBlock;

    public GatheringGoal(MobEntity mob) {
        this.mob = mob;
        this.random = new Random();
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Solo inicia si el Pokémon no tiene una meta y está en un mundo válido.
        if (mob.getNavigation().isIdle() && mob.getWorld() != null) {
            // Busca bloques de bayas o cultivos maduros cercanos.
            List<BlockPos> validBlocks = StreamSupport.stream(BlockPos.iterate(
                mob.getBlockPos().add(-10, -5, -10),
                mob.getBlockPos().add(10, 5, 10)).spliterator(), false)
                .filter(pos -> isBerryOrCrop(mob.getWorld().getBlockState(pos)))
                .map(BlockPos::toImmutable)
                .collect(Collectors.toList());

            if (!validBlocks.isEmpty()) {
                // Elige un bloque al azar para recolectar.
                targetBlock = validBlocks.get(random.nextInt(validBlocks.size()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        // Continúa si el Pokémon está en camino al objetivo y no lo ha alcanzado.
        return targetBlock != null && !mob.getNavigation().isIdle() && mob.getBlockPos().getSquaredDistance(targetBlock) > 2.0;
    }

    @Override
    public void start() {
        // Inicia el movimiento hacia el bloque objetivo.
        if (targetBlock != null) {
            mob.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
        }
    }

    @Override
    public void tick() {
        // Cuando el Pokémon está cerca, simula la recolección.
        if (targetBlock != null && mob.getBlockPos().getSquaredDistance(targetBlock) < 4.0) {
            World world = mob.getWorld();
            BlockState state = world.getBlockState(targetBlock);
            if (isBerryOrCrop(state)) {
                // Aquí podríamos simular la recolección, por ejemplo,
                // soltando los ítems y restableciendo el estado del bloque.
                System.out.println("El Pokémon " + mob.getName().getString() + " ha recolectado en " + targetBlock);
                // Ejemplo de cómo soltar un ítem (requiere más lógica para saber qué soltar).
                world.spawnEntity(new ItemEntity(world, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), new ItemStack(Items.SWEET_BERRIES)));

                // Reiniciar el bloque a una fase inicial.
                if (state.getBlock() instanceof CropBlock) {
                    world.setBlockState(targetBlock, ((CropBlock) state.getBlock()).getDefaultState());
                }
            }
            targetBlock = null; // Resetea la meta.
        }
    }

    private boolean isBerryOrCrop(BlockState state) {
        // Determina si el bloque es un cultivo o una baya y está maduro.
        Block block = state.getBlock();
        // Lógica de ejemplo: solo comprueba si es un cultivo y está en la fase final.
        if (block instanceof CropBlock) {
            return ((CropBlock) block).isMature(state);
        }
        // También podrías añadir otros bloques de bayas específicos aquí.
        return false;
    }
}
