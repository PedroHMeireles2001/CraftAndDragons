package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;

import java.util.Set;

public interface EffectExecutor {
    public void apply(EffectContext context);

    public Set<Effect> getEffects();

    public float getManaCost(float powerMultiplier);

    public String getName();
}
