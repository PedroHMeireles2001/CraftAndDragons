package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.AreaEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.DelayedEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.FilteredEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.RepeatEffectDecorator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record CustomDecoratorConfig(@Nullable Integer delay, @Nullable CustomRepeatConfig repeat , @Nullable Float radius, @Nullable List<CustomFilter> filters) {

    public EffectExecutor wrap(EffectExecutor effectExecutor) {
        EffectExecutor executor = effectExecutor;
        if(Objects.nonNull(filters)){
            for(CustomFilter customFilter : filters){
                long filterTypeLength = filters.stream().filter((filteredFilter -> customFilter.filterType().equals(filteredFilter.filterType()))).count();
                executor = new FilteredEffectDecorator(executor,customFilter.filterType().toString()+"_"+filterTypeLength,customFilter.toPredicate());
            }
        }

        if(Objects.nonNull(repeat)){
            executor = new RepeatEffectDecorator(executor, repeat.times(),repeat.delay());
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
