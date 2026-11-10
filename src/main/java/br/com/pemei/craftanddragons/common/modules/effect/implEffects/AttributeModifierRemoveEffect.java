package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class AttributeModifierRemoveEffect extends Effect {
    private final UUID MODIFIER_UUID;

    private final Attribute attribute;

    public AttributeModifierRemoveEffect(String modifierName, boolean isBeneficial, boolean isFriendlyFire, Supplier<Enchantment> equivalentEnchantment, boolean isSelf, float manaCost, boolean serverSideOnly, Attribute attribute) {
        super(isBeneficial, isFriendlyFire, equivalentEnchantment, isSelf, manaCost, serverSideOnly);
        this.attribute = attribute;
        String modifierNameWithId = "craftanddragons_"+modifierName;
        this.MODIFIER_UUID = UUID.nameUUIDFromBytes(modifierNameWithId.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void applyInternal(EffectContext context) {
        if (context.getCaster().isPresent() && context.getCaster().get() instanceof LivingEntity caster) {
            AttributeInstance armorAttribute = caster.getAttribute(attribute);
            if (Objects.isNull(armorAttribute)) return;

            AttributeModifier currentModifier = armorAttribute.getModifier(MODIFIER_UUID);
            if (Objects.nonNull(currentModifier)) {
                armorAttribute.removeModifier(currentModifier); // Requer a instância AttributeModifier
            }
        }
    }

}
