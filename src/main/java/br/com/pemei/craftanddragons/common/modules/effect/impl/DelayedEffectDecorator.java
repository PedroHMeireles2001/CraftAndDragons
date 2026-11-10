package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.utils.TickScheduler;

public class DelayedEffectDecorator extends AbstractEffectDecorator {
    private final int delay;


    public DelayedEffectDecorator(EffectExecutor wrapped, int delay) {
        super(wrapped);
        this.delay = delay;
    }

    @Override
    public void apply(EffectContext context) {
        TickScheduler.schedule(this.delay,()->super.apply(context));
    }
}
