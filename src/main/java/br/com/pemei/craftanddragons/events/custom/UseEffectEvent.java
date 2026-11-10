package br.com.pemei.craftanddragons.events.custom;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
@RequiredArgsConstructor
@Getter
public class UseEffectEvent extends Event {
    private final EffectContext context;
    private final Effect effect;
}
