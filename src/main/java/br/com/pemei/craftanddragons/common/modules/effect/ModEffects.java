package br.com.pemei.craftanddragons.common.modules.effect;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.ConditionalDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.LoopDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.AttributeModifierRemoveEffect;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.PermanentAttributeModifierEffect;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.RageEffect;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.VorpalEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ModEffects {
    private static final Map<String, Class<? extends Effect>> effectsBase = new HashMap<>();
    private static Map<String, EffectExecutor> effects = new HashMap<>();
    private static final Supplier<Enchantment> noEnchantment = ()->null;

    public static EffectExecutor VORPAL_EFFECT = registerEffect("vorpal", VorpalEffect::new);
    public static final RageEffect BARBARIAN_RAGE = new RageEffect();

    public static EffectExecutor ARMORLESS_DEFENSE = new LoopDecorator(
            
            new ConditionalDecorator(
                    new PermanentAttributeModifierEffect("armorless_defense",true,true,noEnchantment,true,0f,true, Attributes.ARMOR, AttributeModifier.Operation.ADDITION,7),
                    (ctx)-> isCasterNotHearingArmor(ctx.caster()),
                    true,
                    true,
                    new AttributeModifierRemoveEffect("armorless_defense",false,true,noEnchantment,true,0f,true,Attributes.ARMOR)
            ),
            20,
            true,
            true
    );

    private static boolean isCasterNotHearingArmor(Entity caster){
        if(caster instanceof LivingEntity casterEntity){
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack stack = casterEntity.getItemBySlot(slot);
                    if (!stack.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public static Optional<Effect> getEffectBase(String effectName, float amount) {
        Class<? extends Effect> effectClass = getEffectBaseClass(effectName).orElseThrow();
        try {
            return Optional.of(effectClass.getConstructor(Float.class).newInstance(amount));
        } catch (NoSuchMethodException e) {
            return getEffectBase(effectName);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Effect> getEffectBase(String effectName) {
        Class<? extends Effect> effectClass = effectsBase.get(effectName);
        try {
            return Optional.of(effectClass.getConstructor().newInstance());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<EffectExecutor> getEffect(String effectName) {
        return Optional.ofNullable(effects.get(effectName));
    }

    private static Optional<Class<? extends Effect>> getEffectBaseClass(String effectName) {
        return Optional.ofNullable(effectsBase.get(effectName));
    }

    public static EffectExecutor registerEffect(String effectName, Supplier<EffectExecutor> effectExecutorSupplier) {
        EffectExecutor effectExecutor = effectExecutorSupplier.get();
        effects.put(effectName, effectExecutor);
        if(effectExecutor instanceof Effect effect){
            effectsBase.put(effectName, effect.getClass());
        }
        return effectExecutor;
    }
}
