package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.LoopDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.impl.RepeatEffectDecorator;

import java.util.Objects;

public record CustomRepeatConfig(Integer delay, Integer times,Boolean applyOnce,Boolean client) {
    public int getDelay() {
        return Objects.nonNull(delay) ? delay : 20;
    }

    public EffectExecutor toDecorator(EffectExecutor executor) {
        if(Objects.nonNull(this.times)) {
            if(Objects.nonNull(this.delay)) {
                return new RepeatEffectDecorator(executor,this.times,this.delay);
            }
        }
        if(Objects.nonNull(this.delay)) {
            boolean applyOnce = Objects.nonNull(this.applyOnce) ? this.applyOnce : false;
            boolean clientSide = Objects.nonNull(this.client) ? this.client : false;
            return new LoopDecorator(executor,this.delay,applyOnce,!clientSide);
        }
        return executor;
    }
}
