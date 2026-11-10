package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.stream.Collectors;

public interface EffectHolder {

    Map<EffectTrigger, List<EffectExecutor>> getEffectsExecutor();

    default void stopAllTimers(){
        this.getEffectsExecutorFlatten().stream()
                .filter(effectExecutor -> effectExecutor instanceof TimerHolder)
                .map(effectExecutor -> (TimerHolder)effectExecutor)
                .forEach(TimerHolder::stopTimer);
    }

    default void stopAllTimers(Entity entity){
        this.getEffectsExecutorFlatten().stream()
                .filter(effectExecutor -> effectExecutor instanceof TimerHolder)
                .map(effectExecutor -> (TimerHolder)effectExecutor)
                .forEach(timerHolder ->  timerHolder.stopTimer(entity));
    }

    default List<EffectExecutor> getEffectsExecutor(EffectTrigger trigger) {
        Map<EffectTrigger, List<EffectExecutor>> map = this.getEffectsExecutor();
        if (map == null) return Collections.emptyList();
        return map.getOrDefault(trigger, Collections.emptyList());
    }


    default List<EffectExecutor> getEffectsExecutorFlatten() {
        List<EffectExecutor> condensedExecutors = new ArrayList<>();
        for(EffectTrigger trigger : this.getEffectsExecutor().keySet()) {
            List<EffectExecutor> list = this.getEffectsExecutor(trigger);
            if(Objects.isNull(list) || list.isEmpty()) continue;
            condensedExecutors.addAll(list);
        }
        return condensedExecutors;
    }

}
