package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaEffectDecorator extends AbstractEffectDecorator {
    private final double radius;

    public AreaEffectDecorator(EffectExecutor executor,double radius) {
        super(executor);
        this.radius = radius;
    }


    @Override
    public void apply(EffectContext context) {
        Level level = context.level();
        Vec3 centerPos = context.hitVec() != null ? context.hitVec() :
                context.getTargetEntity().map(net.minecraft.world.entity.Entity::position)
                        .orElseGet(() -> context.getTargetBlockPos().map(Vec3::atCenterOf).orElse(null));
        double radiusSq = radius * radius;
        if(Objects.isNull(centerPos)) {
            return;
        }

        AABB area = AABB.ofSize(centerPos, radius * 2, radius * 2, radius * 2);
        if (context.getTargetBlockState().isPresent() || context.getTargetBlockPos().isPresent()) {
            forEachBlockInAABB(level, area, radiusSq, (pos,state)->{
                EffectContext subEffectContext = new EffectContext(level, context.effectHolder(), context.trigger() , context.caster(),null,pos,state,context.hitVec(),context.powerMultiplier());
                super.apply(subEffectContext);
            });
        }

        if (context.getTargetEntity().isPresent()) {
            System.out.println("Aplicando em entidades");
            forEachLivingEntityInSphere(level,area,radiusSq,livingEntity -> super.apply(new EffectContext(level, context.effectHolder(), context.trigger(),context.caster(),livingEntity,null,null,context.hitVec(),context.powerMultiplier())));
        }

    }


    private void forEachBlockInAABB(Level level, AABB aabb,double radiusSq, BiConsumer<BlockPos, BlockState> action) {
        BlockPos min = BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos max = BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ);
        Vec3 center = aabb.getCenter();


        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.getCenter().distanceToSqr(center) <= radiusSq) {
                BlockState state = level.getBlockState(pos);
                if (!state.isAir()) {
                    action.accept(pos, state);
                }
            }
        }

    }

    private void forEachLivingEntityInSphere(
            Level level,
            AABB area,
            double radiusSq,
            Consumer<LivingEntity> action
    ) {


        Predicate<LivingEntity> distancePredicate = entity -> entity.distanceToSqr(area.getCenter()) <= radiusSq;

        level.getEntitiesOfClass(LivingEntity.class, area,distancePredicate).forEach(action);

    }

}
