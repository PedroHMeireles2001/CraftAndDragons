package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.world.entity.Entity;

public interface TimerHolder {
    public void stopTimer();
    public void stopTimer(Entity entity);
}
