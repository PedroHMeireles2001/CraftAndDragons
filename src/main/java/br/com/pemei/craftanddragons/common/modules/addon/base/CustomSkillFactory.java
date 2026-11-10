package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillAttribute;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum CustomSkillFactory {
    EFFECT {
        @Override
        public Skill createSkill(String id, String name, String description, Map<String, Object> params) {
            Map<String,List<String>> effects = (Map<String, List<String>>) params.get("effects");
            Map<EffectTrigger, List<EffectExecutor>> parsedEffects = effects.entrySet().stream()
                    .collect(Collectors.toMap(
                            (entry -> EffectTrigger.valueOf(entry.getKey())),
                            (entry -> entry.getValue().stream().map((entryValue -> ModEffects.getEffect(entryValue).orElseThrow())).toList())
                    ));
            return new SkillEffect(id, Component.literal(name),Component.literal(description),parsedEffects);
        }
    },ATTRIBUTE {
        @Override
        public Skill createSkill(String id, String name, String description, Map<String, Object> params) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(params.get("attribute").toString()));
            double value = (double) params.get("value");
            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf((String) params.getOrDefault("operation", "ADDITION"));
            return new SkillAttribute(id, Component.literal(name),Component.literal(description),attribute,value,operation);
        }
    };

    public abstract Skill createSkill(String id, String name, String description,Map<String, Object> params);
}
