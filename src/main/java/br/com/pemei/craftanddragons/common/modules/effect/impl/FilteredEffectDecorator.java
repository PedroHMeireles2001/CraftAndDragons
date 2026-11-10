package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import lombok.Getter;

import java.util.function.Predicate;


public class FilteredEffectDecorator extends AbstractEffectDecorator {
    @Getter
    private final String internalName;
    private final Predicate<EffectContext> filter;

    public FilteredEffectDecorator(EffectExecutor wrapped, String name, Predicate<EffectContext> filter) {
        super(wrapped);
        this.internalName = name;
        this.filter = filter;
    }

    @Override
    public void apply(EffectContext context) {
        if (filter.test(context)) {
            super.apply(context);
        }else{
            System.out.println("Filtro %s negado!".formatted(internalName));
        }
    }

    @Override
    public String getName() {
        return "%s(%s)".formatted(internalName,this.getWrapped().getName());
    }
}