package br.com.pemei.craftanddragons.common.modules.effect.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.AbstractEffectDecorator;
import br.com.pemei.craftanddragons.common.modules.effect.base.ConditionalTimedEffect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.base.TimerHolder;
import br.com.pemei.craftanddragons.common.utils.TickScheduler;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoopDecorator extends AbstractEffectDecorator implements TimerHolder {
    private final int delay;
    private Map<UUID,Integer> taskId = new ConcurrentHashMap<>();
    private final boolean applyOnce;

    public LoopDecorator(EffectExecutor wrapped, int delay, boolean applyOnce, boolean serverSideOnly) {
        super(wrapped,serverSideOnly);
        this.delay = delay;
        this.applyOnce = applyOnce;
    }

    @Override
    public void apply(EffectContext context) {
        UUID casterId = context.caster().getUUID();
        if(applyOnce && this.taskId.containsKey(casterId)) {
            return;
        }
        if (!taskId.containsKey(casterId)) {
            int newId = TickScheduler.schedule(this.delay,()->{
                if (!context.caster().isAlive()) {
                    stopTimer(context.caster());
                    return;
                }
                super.apply(context);
            },true);
            taskId.put(casterId, newId);
            super.apply(context);
        }
    }

    @Override
    public void stopTimer() {
        this.taskId.values().forEach(TickScheduler::remove);
        this.taskId.clear();
    }

    @Override
    public void stopTimer(Entity entity) {
        UUID entityId = entity.getUUID();
        TickScheduler.remove(this.taskId.get(entityId));
        this.taskId.remove(entityId);
    }
}
