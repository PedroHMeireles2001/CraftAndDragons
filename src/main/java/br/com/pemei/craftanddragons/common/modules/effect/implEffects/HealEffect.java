package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.entity.LivingEntity;

public class HealEffect extends Effect {
    private final float amount;
    public HealEffect(float amount, boolean self,float manaCost) {
        super(true, true, null, self,manaCost,true);
        this.amount = amount;
    }

    @Override
    protected void applyInternal(EffectContext context) {
        if (super.getEntityTarget(context).isEmpty()) {
            return;
        }
        LivingEntity target = super.getEntityTarget(context).get();
        target.heal(this.amount * context.powerMultiplier());
    }

}
