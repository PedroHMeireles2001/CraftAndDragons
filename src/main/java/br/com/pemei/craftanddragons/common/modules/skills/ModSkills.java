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
import java.util.NavigableMap;
import java.util.TreeMap;

public class ModSkills {
    public static final Map<String, Skill> REGISTRY = new HashMap<>();
    private static final Map<String, NavigableMap<Integer, Skill>> SKILL_LEVELS = new HashMap<>();
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
        register(ARMORLESS_DEFENSE);
        register(BARBARIAN_RAGE);
    }

    public static void register(Skill skill) {
        var levels = SKILL_LEVELS.computeIfAbsent(skill.getId(), id -> new TreeMap<>());
        levels.put(skill.getLevel(), skill);
        REGISTRY.put(skill.getId(), levels.lastEntry().getValue());
    }

    public static Skill getSkill(String id, int level) {
        Skill current = REGISTRY.get(id);
        if (current != null && current.getLevel() == level) return current;
        var levels = SKILL_LEVELS.get(id);
        return levels == null ? null : levels.get(level);
    }
}
