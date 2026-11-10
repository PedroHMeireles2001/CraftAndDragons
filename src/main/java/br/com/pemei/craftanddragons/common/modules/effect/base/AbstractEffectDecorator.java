package br.com.pemei.craftanddragons.common.modules.effect.base;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public abstract class AbstractEffectDecorator implements EffectExecutor {
    @Getter
    private final EffectExecutor wrapped;
    private final boolean serverSideOnly;

    @Override
    public void apply(EffectContext context) {
        if(serverSideOnly && context.level().isClientSide())
            return;
        wrapped.apply(context);
    }

    public AbstractEffectDecorator(EffectExecutor wrapped) {
        this.wrapped = wrapped;
        this.serverSideOnly = true;
    }

    @Override
    public float getManaCost(float powerMultiplier) {
        return this.wrapped.getManaCost(powerMultiplier);
    }

    @Override
    public Set<Effect> getEffects() {
        return wrapped.getEffects();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "(" + wrapped.getName() + ")";
    }
}