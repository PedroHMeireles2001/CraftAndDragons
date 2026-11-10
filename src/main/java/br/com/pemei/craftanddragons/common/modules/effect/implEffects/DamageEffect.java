package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

public class DamageEffect extends Effect {

    private final float amount;

    public DamageEffect(float amount, boolean friendlyFire,boolean self,float manaCost) {
        super(false, friendlyFire,()-> Enchantments.SHARPNESS, self,manaCost,true);
        this.amount = amount;
    }

    @Override
    protected void applyInternal(EffectContext context) {
        if (super.getEntityTarget(context).isEmpty()) {
            return;
        }
        LivingEntity target = super.getEntityTarget(context).get();
        DamageSource damageSource;
        Optional<Entity> casterOpt = context.getCaster();
        if (casterOpt.isPresent()) {
            Entity caster = casterOpt.get();
            if(caster instanceof Player player){
                damageSource = target.level().damageSources().playerAttack(player);
            }else{
                damageSource = target.level().damageSources().indirectMagic(caster, caster);
            }
        } else {
            damageSource = target.level().damageSources().generic();
        }
        target.hurt(damageSource,this.amount * context.powerMultiplier());
    }

}
