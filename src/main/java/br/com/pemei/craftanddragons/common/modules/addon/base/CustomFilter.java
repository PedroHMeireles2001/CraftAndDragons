package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.ConditionalDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;
import br.com.pemei.craftanddragons.common.modules.effect.impl.FilteredEffectDecorator;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public record CustomFilter(CustomFilterStrategy filterType, @Nullable Boolean expectedResult, @OptionalParam CustomComparator comparator, @OptionalParam Number expectedValue, @OptionalParam String mod, @OptionalParam
                           EntityCategory expectedCategory, @Nullable CustomEffect orElse, @Nullable Boolean isClient, @Nullable Boolean applyOnce) {

    public Predicate<EffectContext> toPredicate() {
        boolean expectedResult = Objects.nonNull(this.expectedResult) ? this.expectedResult : true;
        return (context -> filterType.applySafe(expectedResult,context, JsonAddonLoader.extractParams(this)));
    }

    public EffectExecutor toDecorator(EffectExecutor executor, long filterTypeLength) {
        if(Objects.nonNull(this.orElse)){
            return new ConditionalDecorator(executor,this.toPredicate(), Boolean.FALSE.equals(this.isClient), Boolean.TRUE.equals(this.applyOnce),this.orElse.getExecutor());
        }
        return new FilteredEffectDecorator(executor,this.filterType().toString()+"_"+filterTypeLength,this.toPredicate());

    }
}
