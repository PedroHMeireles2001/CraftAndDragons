package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FireEffect extends Effect {
    private final int duration;
    private final boolean destructive;
    public FireEffect(boolean isFriendlyFire, boolean isSelf, float manaCost, int duration, boolean destructive) {
        super(false,isFriendlyFire,()->Enchantments.FIRE_ASPECT, isSelf, manaCost, true);
        this.duration = duration;
        this.destructive = destructive;
    }

    @Override
    protected void applyInternal(EffectContext context) {
        super.getEntityTarget(context).ifPresent(entityTarget -> {
            entityTarget.setSecondsOnFire(duration);
        });

        if((context.getTargetBlockPos().isPresent()) && destructive){
            BlockPos targetPos = context.getTargetBlockPos().get();

            BlockState targetState = context.getTargetBlockState()
                    .orElseGet(() -> context.level().getBlockState(targetPos));

            BlockPos firePos = targetState.canBeReplaced() ? targetPos : targetPos.above();
            BlockState firePosState = context.level().getBlockState(firePos);

            if (firePosState.isAir() || firePosState.canBeReplaced()) {
                BlockState fireState = BaseFireBlock.getState(context.level(), firePos);
                context.level().setBlock(firePos, fireState, 3); // Flag 3 = atualiza o bloco e notifica os clientes
            }
        }
    }
}
