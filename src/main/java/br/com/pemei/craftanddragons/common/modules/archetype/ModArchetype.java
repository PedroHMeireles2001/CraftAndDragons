package br.com.pemei.craftanddragons.common.modules.archetype;

import br.com.pemei.craftanddragons.common.modules.archetype.impl.PrincipalArchetype;
import br.com.pemei.craftanddragons.common.modules.archetype.impl.SubArchetype;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;

import java.util.*;

public class ModArchetype {
    public static List<Double> xpRequiredForNextLevel;
    private static Map<String, PrincipalArchetype> archetypes = new HashMap<>();
    private static Map<String, SubArchetype> subarchetypes = new HashMap<>();

    public static void init() {
        archetypes.clear();
        subarchetypes.clear();
        xpRequiredForNextLevel = List.of(0d,300d,900d,2700d,6500d,14000d);
        registerArchetype("barbarian", new PrincipalArchetype("barbarian", "Barbarian",
                "craftanddragons:textures/gui/archetypes/barbarian.png",
                "Raised where steel cracks in the cold, the Barbarian strides into battle in furs. "
                        + "Years of hardship have made their body a shield of its own.",
                3, Map.of(1, Set.of(ModSkills.ARMORLESS_DEFENSE, ModSkills.BARBARIAN_RAGE)), Set.of(), false, 100f, 20));
    }

    public static Set<String> getArchetypeNames() {
        return archetypes.keySet();
    }

    public static PrincipalArchetype getArchetype(String archetypeId) {
        if(Objects.isNull(archetypes.get(archetypeId)) || archetypeId.isEmpty()){
            return null;
        }
        return archetypes.get(archetypeId);
    }

    public static SubArchetype getSubArchetype(String mainSubArchetype) {
        if(Objects.isNull(mainSubArchetype) || mainSubArchetype.isEmpty()){
            return null;
        }
        return subarchetypes.get(mainSubArchetype);
    }

    public static void registerArchetype(String archetypeId, PrincipalArchetype archetype) {
        archetypes.put(archetypeId, archetype);
        archetype.getSkillsPerLevel().values().forEach(skills -> skills.forEach(ModSkills::register));
        archetype.getSubArchetypes().forEach(subClass ->
                subClass.getSkillsPerLevel().values().forEach(skills -> skills.forEach(ModSkills::register)));
    }
}
