package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class PotionGiverEffect extends Effect {
    private final int duration;
    private final int amplifier;
    private final MobEffect potionEffect;
    public PotionGiverEffect(boolean isBeneficial, boolean friendlyFire, boolean self, int duration, int amplifier, MobEffect potionEffect,float manaCost) {
        super(isBeneficial,friendlyFire, null,self,manaCost,true);
        this.duration = duration;
        this.amplifier = amplifier;
        this.potionEffect = potionEffect;
    }

    @Override
    protected void applyInternal(EffectContext context) {
        if (super.getEntityTarget(context).isEmpty()) {
            return;
        }
        LivingEntity target = super.getEntityTarget(context).get();
        target.addEffect(new MobEffectInstance(this.potionEffect, this.duration, this.amplifier));
    }

}
