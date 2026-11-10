package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public record EffectContext(
        Level level,
        EffectHolder effectHolder,
        EffectTrigger trigger,
        Entity caster,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockPos targetBlockPos,
        @Nullable BlockState targetBlockState,
        @Nullable Vec3 hitVec,
        float powerMultiplier
) {
    public Optional<LivingEntity> getTargetEntity() { return Optional.ofNullable(targetEntity); }
    public Optional<BlockPos> getTargetBlockPos() { return Optional.ofNullable(targetBlockPos); }
    public Optional<BlockState> getTargetBlockState() { return Optional.ofNullable(targetBlockState); }
    public Optional<Entity> getCaster() { return Optional.ofNullable(caster); }
}