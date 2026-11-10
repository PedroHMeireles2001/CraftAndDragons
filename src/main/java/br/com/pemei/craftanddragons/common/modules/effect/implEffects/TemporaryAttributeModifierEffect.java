package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.TemporaryEffect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class TemporaryAttributeModifierEffect extends TemporaryEffect {
    private final Attribute attribute;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final UUID modifierId;

    public TemporaryAttributeModifierEffect(boolean isBeneficial, boolean isFriendlyFire, boolean isSelf, int duration, Attribute attribute, float value, AttributeModifier.Operation operation, float manaCost) {
        super(isBeneficial, isFriendlyFire, null, isSelf, duration,manaCost,true);
        this.attribute = attribute;
        this.value = value;
        this.operation = operation;
        this.modifierId = UUID.nameUUIDFromBytes(
                ("CD_TempAttr_" + attribute.getDescriptionId() + "_" + value).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void applyStart(EffectContext context) {
        if (super.getEntityTarget(context).isEmpty()) {
            return;
        }
        LivingEntity target = super.getEntityTarget(context).get();
        AttributeInstance attributeInstance = target.getAttribute(this.attribute);
        if(Objects.isNull(attributeInstance)) {
            return;
        }

        AttributeModifier modifier = new AttributeModifier(
                this.modifierId,
                "CraftAndDragons Temporary Attribute",
                this.value,
                this.operation
        );

        // Garante que o modificador não seja duplicado se o efeito for re-aplicado
        if (!attributeInstance.hasModifier(modifier)) {
            attributeInstance.addTransientModifier(modifier);
        }
    }

    @Override
    protected void applyEnd(EffectContext context) {
        if (context.getTargetEntity().isEmpty()) {
            return;
        }

        LivingEntity target = context.getTargetEntity().get();
        AttributeInstance attributeInstance = target.getAttribute(this.attribute);

        if (Objects.nonNull(attributeInstance)) {
            attributeInstance.removeModifier(this.modifierId);
        }
    }
}
