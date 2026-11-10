package br.com.pemei.craftanddragons.common.modules.addon.base;


import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;
import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;

import javax.annotation.Nullable;
import java.util.*;

public record CustomEffect(@Nullable String name,@Nullable String registerType,@Nullable String type, float manaCost,@Nullable CustomDecoratorConfig decorators,@OptionalParam Float amount,@OptionalParam Integer duration,@OptionalParam Integer amplifier,@OptionalParam String potionEffect,@OptionalParam Boolean isFriendlyFire, @OptionalParam Boolean isSelf, @OptionalParam Boolean isBeneficial,@OptionalParam String operation, @OptionalParam String attribute) {


    public EffectExecutor getExecutor() {
        if(Objects.nonNull(registerType)){
            Optional<EffectExecutor> effectOpt = ModEffects.getEffect(registerType);
            if(effectOpt.isPresent()){
                return effectOpt.get();
            }
        }
        assert this.type != null;
        CustomEffectFactory customEffectFactory = CustomEffectFactory.valueOf(this.type.toUpperCase());
        Map<String,Object> params = JsonAddonLoader.extractParams(this);
        EffectExecutor executor = customEffectFactory.create(manaCost,params);
        if(Objects.nonNull(decorators)) {
            executor = decorators.wrap(executor);
        }


        return executor;
    }
    public String getName(){
        return Objects.isNull(this.name) ? this.type+"_"+ UUID.randomUUID().toString() : this.name;
    }
}
