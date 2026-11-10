package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EffectChain implements EffectExecutor {
    private final List<EffectExecutor> executors;
    @Override
    public void apply(EffectContext context) {
        executors.forEach(executor -> executor.apply(context));
    }

    @Override
    public Set<Effect> getEffects() {
        return executors.stream().map(EffectExecutor::getEffects).flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public float getManaCost(float powerMultiplier) {
        return (float) executors.stream().mapToDouble(executor->executor.getManaCost(powerMultiplier)).sum();
    }

    @Override
    public String getName() {
        return "Chain(" + executors.stream().map(EffectExecutor::getName).collect(Collectors.joining(", ")) + ")";
    }
}
