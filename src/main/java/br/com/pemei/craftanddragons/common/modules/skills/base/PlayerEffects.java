package br.com.pemei.craftanddragons.common.modules.skills.base;


import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;
import org.jetbrains.annotations.NotNull;

public record PlayerEffects(EffectTrigger trigger, Skill holder, EffectExecutor executor, boolean enabled) {
    public PlayerEffects(String[] chunks, boolean enabled) {
        this(EffectTrigger.valueOf(chunks[0]), ModSkills.REGISTRY.get(chunks[1]), ModEffects.getEffect(chunks[2]).orElseThrow(), enabled);
    }

    @Override
    public @NotNull String toString() {
        return this.trigger.toString()+";"+this.holder.toString()+";"+this.executor.toString();
    }
}
