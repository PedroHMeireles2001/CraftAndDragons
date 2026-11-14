package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.AreaEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.DelayedEffectDecorator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record CustomDecoratorConfig(@Nullable Integer delay, @Nullable CustomRepeatConfig repeat , @Nullable Float radius, @Nullable List<CustomFilter> filters) {

    public EffectExecutor toDecorator(EffectExecutor effectExecutor) {
        EffectExecutor executor = effectExecutor;
        if(Objects.nonNull(filters)){
            for(CustomFilter customFilter : filters){
                long filterTypeLength = filters.stream().filter((filteredFilter -> customFilter.filterType().equals(filteredFilter.filterType()))).count();
                executor = customFilter.toDecorator(executor,filterTypeLength);
            }
        }

        if(Objects.nonNull(repeat)){
            executor = repeat.toDecorator(executor);
        }
        if(Objects.nonNull(delay)){
            executor = new DelayedEffectDecorator(executor, delay);
        }
        if(Objects.nonNull(radius)){
            executor = new AreaEffectDecorator(executor, radius);
        }
        return executor;
    }
}
