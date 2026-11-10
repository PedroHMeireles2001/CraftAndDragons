package br.com.pemei.craftanddragons.common.modules.skills;

import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModSkills {
    public static final Map<String, Skill> REGISTRY = new HashMap<>();
    public static final Skill ARMORLESS_DEFENSE = new SkillEffect("skill_armorlessdefense",
            Component.translatable("skill.craftanddragons.armorless_defense.name"),
            Component.translatable("skill.craftanddragons.armorless_defense.description"),
            Map.of(EffectTrigger.ON_SKILL_APPLY, List.of(ModEffects.ARMORLESS_DEFENSE)));
}
