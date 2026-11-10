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

public class PermanentAttributeModifierEffect extends Effect {
    private final UUID MODIFIER_UUID;

    private final Attribute attribute;
    private final AttributeModifier.Operation operation;
    private final double amount;

    public PermanentAttributeModifierEffect(String modifierName,boolean isBeneficial, boolean isFriendlyFire, Supplier<Enchantment> equivalentEnchantment, boolean isSelf, float manaCost, boolean serverSideOnly, Attribute attribute, AttributeModifier.Operation operation, double amount) {
        super(isBeneficial, isFriendlyFire, equivalentEnchantment, isSelf, manaCost, serverSideOnly);
        this.attribute = attribute;
        this.operation = operation;
        this.amount = amount;
        String modifierNameWithId = "craftanddragons_"+modifierName;
        this.MODIFIER_UUID = UUID.nameUUIDFromBytes(modifierNameWithId.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void applyInternal(EffectContext context) {
        if (context.getCaster().isPresent() && context.getCaster().get() instanceof LivingEntity caster) {
            AttributeInstance armorAttribute = caster.getAttribute(Attributes.ARMOR);
            if (Objects.isNull(armorAttribute)) return;

            AttributeModifier armorlessDefenseModifier = new AttributeModifier(
                    MODIFIER_UUID,
                    "Armorless Defense",
                    amount,
                    AttributeModifier.Operation.ADDITION
            );

            // Busca o modificador atual registrado com esse UUID
            AttributeModifier currentModifier = armorAttribute.getModifier(MODIFIER_UUID);

            if (Objects.nonNull(currentModifier)) {
                // Se o novo modificador for mais forte que o atual, remove a instância atual e adiciona a nova
                if (calculateEffectivePower(armorAttribute, currentModifier) < calculateEffectivePower(armorAttribute, armorlessDefenseModifier)) {
                    armorAttribute.removeModifier(currentModifier); // Requer a instância AttributeModifier
                    armorAttribute.addPermanentModifier(armorlessDefenseModifier);
                }
            } else {
                // Se não possui nenhum modificador com este UUID, adiciona
                armorAttribute.addPermanentModifier(armorlessDefenseModifier);
            }
        }
    }

    private double calculateEffectivePower(AttributeInstance attribute, AttributeModifier modifier) {
        return switch (modifier.getOperation()) {
            case ADDITION ->
                // Na adição, o poder é o próprio valor bruto (ex: +4 armadura = 4 pontos)
                    modifier.getAmount();
            case MULTIPLY_BASE ->
                // Na multiply base, o ganho depende do valor base do atributo (ex: base 2 * 0.1 = 0.2 pontos)
                    attribute.getBaseValue() * modifier.getAmount();
            case MULTIPLY_TOTAL ->
                // Na multiply total, o ganho é sobre o total acumulado (aproximamos usando a base)
                    attribute.getValue() * modifier.getAmount();
            default -> modifier.getAmount();
        };
    }
}
