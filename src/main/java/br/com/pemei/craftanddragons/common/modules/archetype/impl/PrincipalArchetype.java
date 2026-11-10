package br.com.pemei.craftanddragons.common.modules.archetype.impl;

import br.com.pemei.craftanddragons.common.modules.archetype.base.Archetype;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class PrincipalArchetype extends Archetype {
    private final Set<SubArchetype> subArchetypes;
    private final int subClassLevel;
    private final boolean isMagic;
    private final float baseMana;
    private final float baseHealth;

    public PrincipalArchetype(String id,String name, String description, int subClassLevel, Map<Integer, Set<Skill>> skillsPerLevel, Set<SubArchetype> subArchetypes, boolean isMagic, float baseMana, float baseHealth) {
        super(id,name, description, skillsPerLevel);
        this.subArchetypes = subArchetypes;
        this.subClassLevel = subClassLevel;
        this.isMagic = isMagic;
        this.baseMana = baseMana;
        this.baseHealth = baseHealth;
    }
}
