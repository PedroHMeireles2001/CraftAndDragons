package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.effect.base.TemporaryEffect;
import br.com.pemei.craftanddragons.common.modules.effect.base.TimerHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.Map;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class RageEffect extends TemporaryEffect implements TimerHolder {
    public static final int DURATION_SECONDS = 10;
    public static final int MELEE_BONUS = 2;
    public static final int PHYSICAL_REDUCTION = 50;
    private final Map<ServerPlayer, Rage> rageByPlayer = new IdentityHashMap<>();

    private record Rage(EffectContext context, long expiry) {}

    public RageEffect() {
        super(true, true, null, true, DURATION_SECONDS * 20, 30, true);
    }

    @Override
    protected void applyStart(EffectContext context) {
        if (!(context.caster() instanceof ServerPlayer player)) return;

        rageByPlayer.put(player, new Rage(context, player.serverLevel().getGameTime() + getDurationTicks()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR,
                player.getSoundSource(), 0.65f, 1.05f);
        player.serverLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(),
                player.getY() + 1.1, player.getZ(), 12, 0.35, 0.45, 0.35, 0);
    }

    @Override
    protected void applyEnd(EffectContext context) {
        // O contexto identifica a mesma ativação mesmo se o jogador foi substituído ao voltar do End.
        rageByPlayer.values().removeIf(rage -> rage.context() == context);
    }

    @Override
    public int getRemainingTicks(Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return 0;
        Rage rage = rageByPlayer.get(player);
        if (rage == null) return 0;

        long remaining = rage.expiry() - player.serverLevel().getGameTime();
        if (remaining <= 0) {
            stopTimer(player);
            return 0;
        }
        return (int) remaining;
    }

    @Override
    public void stopTimer() {
        rageByPlayer.clear();
    }

    @Override
    public void stopTimer(Entity entity) {
        rageByPlayer.remove(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original)
                || !(event.getEntity() instanceof ServerPlayer replacement)) return;

        var rageByPlayer = ModEffects.BARBARIAN_RAGE.rageByPlayer;
        Rage rage = rageByPlayer.remove(original);
        if (!event.isWasDeath() && rage != null && rage.expiry() > replacement.serverLevel().getGameTime()) {
            rageByPlayer.put(replacement, rage);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModEffects.BARBARIAN_RAGE.stopTimer(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (amount <= 0) return;

        if (source.is(DamageTypes.PLAYER_ATTACK) && source.getEntity() instanceof ServerPlayer attacker
                && ModEffects.BARBARIAN_RAGE.getRemainingTicks(attacker) > 0) {
            amount += MELEE_BONUS;
        }

        if (event.getEntity() instanceof ServerPlayer target && isPhysicalAttack(source)
                && ModEffects.BARBARIAN_RAGE.getRemainingTicks(target) > 0) {
            amount *= (100 - PHYSICAL_REDUCTION) / 100f;
        }
        event.setAmount(amount);
    }

    private static boolean isPhysicalAttack(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_RESISTANCE) && (source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(DamageTypes.STING) || source.is(DamageTypeTags.IS_PROJECTILE));
    }
}
