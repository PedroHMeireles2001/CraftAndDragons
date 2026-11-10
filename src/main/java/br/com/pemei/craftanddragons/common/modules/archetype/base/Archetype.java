package br.com.pemei.craftanddragons.common.modules.archetype.base;

import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public abstract class Archetype {
    private final String id;
    private final String name;
    private final String description;
    private final Map<Integer, Set<Skill>> skillsPerLevel;

}
