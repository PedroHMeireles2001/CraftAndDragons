package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record CustomSkill(@Nullable String id, String name, String description,CustomSkillFactory type,@OptionalParam Map<String, List<String>> effects, @OptionalParam String attribute, @OptionalParam String operation, @OptionalParam Double value) {
    public Skill toSkill(){
        Map<String,Object> params = JsonAddonLoader.extractParams(this);
        return type.createSkill(getId(),name,description,params);
    }

    public String getId(){
        return Objects.nonNull(id) ? id : this.name().toLowerCase().replaceAll(" ", "_");
    }
}
