package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.archetype.impl.PrincipalArchetype;
import br.com.pemei.craftanddragons.common.modules.archetype.impl.SubArchetype;
import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record CustomSkill(@Nullable String id, String name, String description,CustomSkillFactory type,@OptionalParam Map<String, List<String>> effects, @OptionalParam String attribute, @OptionalParam String operation, @OptionalParam Double value, @OptionalParam Integer level) {
    public CustomSkill(String id, String name, String description, CustomSkillFactory type,
                       Map<String, List<String>> effects, String attribute, String operation, Double value) {
        this(id, name, description, type, effects, attribute, operation, value, null);
    }

    public Skill toSkill(){
        Map<String,Object> params = JsonAddonLoader.extractParams(this);
        return type.createSkill(getId(),name,description,params);
    }

    public String getId(){
        return Objects.nonNull(id) ? id : this.name().toLowerCase().replaceAll(" ", "_");
    }

    public static PrincipalArchetype toClass(CustomClass customClass) {
        Set<SubArchetype> subClasses = customClass.subClasses() == null ? Set.of()
                : customClass.subClasses().stream()
                        .map(subClass -> new SubArchetype(subClass.name(), subClass.id(), subClass.iconPath(),
                                subClass.description(), subClass.skills() == null ? Map.of() : subClass.skills()))
                        .collect(Collectors.toSet());
        return new PrincipalArchetype(customClass.id(), customClass.name(), customClass.iconPath(),
                customClass.description(),
                customClass.subclassUnlockLevel() == null ? 3 : customClass.subclassUnlockLevel(),
                customClass.skills() == null ? Map.of() : customClass.skills(), subClasses,
                Boolean.TRUE.equals(customClass.isMagic()),
                customClass.baseMana() == null ? 0f : customClass.baseMana(),
                customClass.baseHealth() == null ? 20f : customClass.baseHealth());
    }
}
