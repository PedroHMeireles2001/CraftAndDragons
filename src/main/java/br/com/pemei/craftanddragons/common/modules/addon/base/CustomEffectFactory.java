package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public enum CustomEffectFactory {
    POTION_GIVER {
        @Override
        public Effect create(float manaCost, Map<String, Object> params) {
            ResourceLocation location = ResourceLocation.tryParse((String) params.get("potionEffect"));
            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(location);
            assert mobEffect != null;
            boolean isBeneficial = mobEffect.isBeneficial();
            boolean friendlyFire = (boolean) params.getOrDefault("isFriendlyFire",isBeneficial);
            boolean isSelf = (boolean) params.getOrDefault("isSelf",false);
            int duration = (Integer) params.get("duration");
            int amplifier = (Integer) params.getOrDefault("amplifier",1);
            return new PotionGiverEffect(isBeneficial,friendlyFire,isSelf,duration,amplifier,mobEffect,manaCost);
        }
    },DAMAGE {
        @Override
        public Effect create(float manaCost, Map<String, Object> params) {
            boolean isSelf = (boolean) params.getOrDefault("isSelf",false);
            boolean friendlyFire = (boolean) params.getOrDefault("isFriendlyFire",false);
            float amount = (Float) params.get("amount");
            return new DamageEffect(amount,friendlyFire,isSelf,manaCost);
        }
    },HEAL {
        @Override
        public Effect create(float manaCost, Map<String, Object> params) {
            boolean isSelf = (boolean) params.getOrDefault("isSelf",false);
            float amount = (Float) params.get("amount");
            return new HealEffect(amount,isSelf,manaCost);
        }
    }, TEMPORARY_ATTRIBUTE_MODIFIER {
        @Override
        public Effect create(float manaCost, Map<String, Object> params) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse((String) params.get("attribute")));
            int duration = (Integer) params.get("duration");
            float amount = (Float) params.get("amount");
            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(params.get("operation").toString());
            boolean isBeneficialDefault = (amount > 0 && operation == AttributeModifier.Operation.ADDITION) || (amount >= 1 && (operation == AttributeModifier.Operation.MULTIPLY_BASE || operation == AttributeModifier.Operation.MULTIPLY_TOTAL));
            boolean isBeneficial = (boolean) params.getOrDefault("isBeneficial", isBeneficialDefault);
            boolean friendlyFire = (boolean) params.getOrDefault("isFriendlyFire",false);
            boolean isSelf = (boolean) params.getOrDefault("isSelf",true);
            assert attribute != null;
            return new TemporaryAttributeModifierEffect(isBeneficial,friendlyFire,isSelf,duration,attribute,amount,operation,manaCost);
        }
    }, FIRE {
        @Override
        public Effect create(float manaCost, Map<String, Object> params) {
            boolean friendlyFire = (boolean) params.getOrDefault("isFriendlyFire",false);
            boolean isSelf = (boolean) params.getOrDefault("isSelf",false);
            boolean destructive = (boolean) params.getOrDefault("isSelf",false);
            int duration = (Integer) params.get("duration");

            return new FireEffect(friendlyFire,isSelf,manaCost,duration,destructive);
        }
    };

    public abstract Effect create(float manaCost,Map<String, Object> params);
}
