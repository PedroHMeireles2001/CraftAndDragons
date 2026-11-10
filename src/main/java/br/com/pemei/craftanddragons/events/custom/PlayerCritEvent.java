package br.com.pemei.craftanddragons.events.custom;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
@RequiredArgsConstructor
public class PlayerCritEvent extends Event {
    private final Player attacker;
    private final LivingEntity target;
    private final float result;
    private final double finalDamage;

}
