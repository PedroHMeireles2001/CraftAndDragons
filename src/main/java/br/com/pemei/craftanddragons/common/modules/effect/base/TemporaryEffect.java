package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.utils.TickScheduler;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

public abstract class TemporaryEffect extends Effect {
    private final int duration;
    public TemporaryEffect(boolean isBeneficial, boolean isFriendlyFire, Supplier<Enchantment> equivalentEnchantment, boolean isSelf, int duration, float manaCost, boolean serverSide) {
        super(isBeneficial, isFriendlyFire, equivalentEnchantment, isSelf,manaCost, serverSide);
        this.duration = duration;
    }

    @Override
    protected void applyInternal(EffectContext context) {
        applyStart(context);
        TickScheduler.schedule(this.duration,()->applyEnd(context));
    }

    protected abstract void applyStart(EffectContext context);
    protected abstract void applyEnd(EffectContext context);
}
