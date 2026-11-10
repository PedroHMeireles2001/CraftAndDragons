package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.ConditionalTimedEffect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ConditionalDecorator extends AbstractEffectDecorator {
    private final Predicate<EffectContext> condition;
    private final Map<UUID, Boolean> status = new ConcurrentHashMap<>();
    private final boolean applyOnce;
    private final EffectExecutor wrappedFalse;

    public ConditionalDecorator(EffectExecutor wrapped, Predicate<EffectContext> condition, boolean serverSideOnly, boolean applyOnce, EffectExecutor wrappedFalse) {
        super(wrapped,serverSideOnly);
        this.condition = condition;
        this.applyOnce = applyOnce;
        this.wrappedFalse = wrappedFalse;
    }

    @Override
    public void apply(EffectContext context) {
        UUID casterId = context.caster().getUUID();
        if(condition.test(context)) {
            if(applyOnce && this.status.containsKey(casterId) && this.status.get(casterId)) {
                return;
            }
            super.apply(context);
        }else{
            if(Objects.isNull(wrappedFalse) || (applyOnce && this.status.containsKey(casterId) && !this.status.get(casterId))) {
                return;
            }
            wrappedFalse.apply(context);
        }
    }
}
