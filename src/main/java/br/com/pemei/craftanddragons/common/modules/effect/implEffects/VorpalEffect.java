package br.com.pemei.craftanddragons.common.modules.effect.implEffects;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.core.init.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static br.com.pemei.craftanddragons.core.init.ModItems.DECAPTATION_MAP;

public class VorpalEffect extends Effect {

    public VorpalEffect() {
        super(false, true, ModEnchantments.VORPAL_ENCHANTMENT, false,0,true);
    }


    @Override
    protected void applyInternal(EffectContext context) {
        if(context.getTargetEntity().isEmpty())
            return;

        LivingEntity target = context.getTargetEntity().get();

        if(!DECAPTATION_MAP.containsKey(target.getType()))
            return;

        double x = target.getX();
        double y = target.getY() + 1.0;
        double z = target.getZ();


        target.kill();
        ItemStack itemDrop = null;
        Level level = target.level();
        if(target instanceof Player victim) {
            ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);

            // Pega o nome real do jogador vitimado
            String victimName = victim.getGameProfile().getName();

            // Injeta a NBT Tag "SkullOwner" para o jogo baixar a skin dele
            playerHead.getOrCreateTag().putString("SkullOwner", victimName);

            itemDrop = playerHead;


        }else{
            itemDrop = new ItemStack(DECAPTATION_MAP.getOrDefault(target.getType(),Items.ZOMBIE_HEAD));
        }


        ItemEntity drop = new ItemEntity(
                target.level(),
                target.getX(), target.getY(), target.getZ(),
                itemDrop
        );

        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
        level.playSound(null, x, y, z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (level instanceof ServerLevel serverLevel) {

            // Explosão de magia negra/crítico (15 partículas espalhadas)
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    x, y, z,
                    15,           // Quantidade de partículas
                    0.5, 0.5, 0.5, // Dispersão (Spread) em X, Y, Z
                    0.2           // Velocidade extra da partícula
            );

            // Partícula do corte largo da espada (Sweep)
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    x, y, z,
                    1, 0, 0, 0, 0
            );
        }


    }
}
