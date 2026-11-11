package br.com.pemei.craftanddragons.common.modules.skills;

import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.RageEffect;
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
    public static final SkillEffect BARBARIAN_RAGE = new SkillEffect("barbarian_rage",
            Component.translatable("skill.craftanddragons.barbarian_rage.name"),
            Component.translatable("skill.craftanddragons.barbarian_rage.description",
                    RageEffect.DURATION_SECONDS, RageEffect.MELEE_BONUS, RageEffect.PHYSICAL_REDUCTION),
            Map.of(EffectTrigger.ON_SKILL_USE, List.of(ModEffects.BARBARIAN_RAGE)));

    static {
        REGISTRY.put(BARBARIAN_RAGE.getId(), BARBARIAN_RAGE);
    }
}
