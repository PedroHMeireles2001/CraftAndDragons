package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectChain;
import br.com.pemei.craftanddragons.core.exceptions.HomebrewParseException;
import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record CustomEffectChain(String id,String name,@Nullable String extendsChain, List<CustomEffect> chain,@Nullable CustomDecoratorConfig decorators) {
    private EffectChain toChain() throws HomebrewParseException {
        if(Objects.nonNull(extendsChain) && !extendsChain.isEmpty()){
            if(ModEffects.getEffect(extendsChain).orElseThrow() instanceof EffectChain effectChain){
                return effectChain;
            }else{
                throw new HomebrewParseException("extendsChain must be a effect chain!");
            }
        }
        return new EffectChain(this.chain.stream().map(CustomEffect::getExecutor).toList());
    }
    public EffectExecutor getExecutor() throws HomebrewParseException {
        EffectExecutor executor = toChain();
        if(Objects.nonNull(decorators)) {
            executor = decorators.wrap(executor);
        }
        return executor;
    }
}
