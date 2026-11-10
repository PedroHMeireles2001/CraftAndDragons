package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public record CustomFilter(CustomFilterStrategy filterType, @Nullable Boolean expectedResult, @OptionalParam CustomComparator comparator, @OptionalParam Number expectedValue, @OptionalParam String mod, @OptionalParam
                           EntityCategory expectedCategory) {

    public Predicate<EffectContext> toPredicate() {
        boolean expectedResult = Objects.nonNull(this.expectedResult) ? this.expectedResult : true;
        return (context -> filterType.applySafe(expectedResult,context, JsonAddonLoader.extractParams(this)));
    }

}
