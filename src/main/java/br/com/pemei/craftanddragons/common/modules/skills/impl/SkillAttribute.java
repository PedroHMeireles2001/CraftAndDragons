package br.com.pemei.craftanddragons.common.modules.skills.impl;

import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Getter
public class SkillAttribute extends Skill {
    private final Attribute attribute;
    private final double value;
    private final AttributeModifier.Operation operation;
    private final UUID modifierId;

    public SkillAttribute(String id, Component name, Component description, Attribute attribute, double value, AttributeModifier.Operation operation) {
        super(id, name, description);
        this.attribute = attribute;
        this.value = value;
        this.operation = operation;

        this.modifierId = UUID.nameUUIDFromBytes(("skill_attribute_" + id).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void apply(LivingEntity entity, PlayerSkillsCapability capability) {
        AttributeInstance instance = entity.getAttribute(this.attribute);
        if (instance != null) {
            AttributeModifier modifier = new AttributeModifier(
                    this.modifierId,
                    "skill_" + this.getId(),
                    this.value,
                    this.operation
            );

            if (!instance.hasModifier(modifier)) {
                instance.addPermanentModifier(modifier);
            }
        }
    }

    @Override
    public void remove(LivingEntity entity, PlayerSkillsCapability capability) {
        AttributeInstance instance = entity.getAttribute(this.attribute);

        if (instance != null) {
            instance.removeModifier(this.modifierId);
        }

    }
}