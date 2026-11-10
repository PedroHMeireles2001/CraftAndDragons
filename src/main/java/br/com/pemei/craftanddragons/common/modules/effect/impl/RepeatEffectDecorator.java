package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.utils.TickScheduler;

public class RepeatEffectDecorator extends AbstractEffectDecorator {
    private final int n;
    private final int delay;
    public RepeatEffectDecorator(EffectExecutor wrapped, int n, int delay) {
        super(wrapped);
        this.n = n;
        this.delay = delay;
    }

    @Override
    public void apply(EffectContext context) {
        TickScheduler.schedule(this.delay,this.n,()->super.apply(context));
    }
}
