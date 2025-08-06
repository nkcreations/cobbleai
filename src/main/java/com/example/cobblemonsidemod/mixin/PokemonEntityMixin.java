package com.example.cobblemonsidemod.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.example.cobblemonsidemod.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends PathAwareEntity {

    protected PokemonEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void addCustomGoals(CallbackInfo ci) {
        CobblemonSideMod.LOGGER.info("Injecting custom goals for " + this.getName().getString());

        // Add all custom goals. The goals themselves will check the species.
        this.goalSelector.add(1, new FlyingPathfindGoal(this, CobblemonSideMod.config.flyingMinHeight, CobblemonSideMod.config.flyingMaxHeight));
        this.goalSelector.add(2, new DiggingGoal(this, CobblemonSideMod.config.diggingBlocks, CobblemonSideMod.config.diggingBlockBreakTime));
        this.goalSelector.add(3, new GatheringGoal(this));
        this.goalSelector.add(4, new CuriousGoal(this, 1.0D, CobblemonSideMod.config.curiousFollowDistance));
    }
}
