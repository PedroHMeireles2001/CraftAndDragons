package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.utils.TickScheduler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ConditionalTimedEffect extends Effect implements TimerHolder {
    private final Predicate<EffectContext> condition;
    private Map<UUID,TimedStatus> status = new ConcurrentHashMap<>();
    private Map<UUID,Integer> taskId = new ConcurrentHashMap<>();

    private enum TimedStatus{
        APPLIED, REMOVED, INITIALIZED,STOPPED;
    }

    public ConditionalTimedEffect(boolean isBeneficial, boolean isFriendlyFire, Supplier<Enchantment> equivalentEnchantment, boolean isSelf, float manaCost, boolean serverSideOnly, Predicate<EffectContext> condition) {
        super(isBeneficial, isFriendlyFire, equivalentEnchantment, isSelf, manaCost, serverSideOnly);
        this.condition = condition;
    }



    @Override
    protected void applyInternal(EffectContext context) {

    }

    private void timedApply(EffectContext context) {
        UUID casterId = context.caster().getUUID();
        TimedStatus timedStatus = status.getOrDefault(casterId,TimedStatus.INITIALIZED);
        if (condition.test(context)) {
            if(timedStatus != TimedStatus.APPLIED){
                this.status.put(casterId,TimedStatus.APPLIED);
                applyOnceTrue(context);
            }
        }else{
            if(timedStatus != TimedStatus.REMOVED){
                this.status.put(casterId,TimedStatus.REMOVED);
                applyOnceFalse(context);
            }
        }
    }

    @Override
    public void stopTimer() {
        this.status.clear();
        this.taskId.values().forEach(TickScheduler::remove);
        this.taskId.clear();
    }

    @Override
    public void stopTimer(Entity entity) {
        UUID entityId = entity.getUUID();
        this.status.remove(entityId);
        TickScheduler.remove(this.taskId.get(entityId));
        this.taskId.remove(entityId);
    }

    public abstract void applyOnceTrue(EffectContext context);
    public abstract void applyOnceFalse(EffectContext context);
}
