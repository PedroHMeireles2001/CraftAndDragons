package br.com.pemei.craftanddragons.common.modules.archetype.impl;

import br.com.pemei.craftanddragons.common.modules.archetype.base.Archetype;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;

import java.util.Map;
import java.util.Set;

public class SubArchetype extends Archetype {

    public SubArchetype(String name, String id, String description, Map<Integer, Set<Skill>> skillsPerLevel) {
        super(id,name, description, skillsPerLevel);
    }
}
