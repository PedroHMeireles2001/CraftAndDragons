package br.com.pemei.craftanddragons.common.modules.effect.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.events.custom.UseEffectEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public abstract class Effect implements EffectExecutor {
    private final boolean isBeneficial;
    private final boolean isFriendlyFire;
    private final Supplier<Enchantment> equivalentEnchantment;
    private final boolean isSelf;
    private final float manaCost;
    private final boolean serverSideOnly;
    private final ThreadLocal<Set<UUID>> executingEntities = ThreadLocal.withInitial(HashSet::new);


    public final void apply(EffectContext context){
        if(serverSideOnly && context.level().isClientSide())
            return;

        UUID casterId = context.caster().getUUID();
        if(executingEntities.get().contains(casterId))
            return;

        try{
            if(!dispatchEvent(context,this)){
                executingEntities.get().add(casterId);
                applyInternal(context);
            }
        }finally {
            executingEntities.get().remove(casterId);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    private boolean dispatchEvent(EffectContext context, Effect effect){
        return MinecraftForge.EVENT_BUS.post(new UseEffectEvent(context,effect));
    }



    protected Optional<LivingEntity> getEntityTarget(EffectContext context){
        if(isSelf && context.caster() instanceof LivingEntity caster){
            return Optional.of(caster);
        };
        if(!isSelf){
            return context.getTargetEntity();
        }
        return Optional.empty();
    }

    @Override
    public Set<Effect> getEffects() {
        return Set.of(this);
    }

    protected abstract void applyInternal(EffectContext context);

    @Override
    public float getManaCost(float powerMultiplier) {
        return this.manaCost * powerMultiplier;
    }

    @Override
    public int getRemainingTicks(Entity entity) {
        return 0;
    }

    public enum TargetType {
        ENTITY{

            @Override
            public boolean isValidTarget(EffectContext context,Effect effect) {
                if(context.getTargetEntity().isEmpty())
                    return false;

                Entity target = context.getTargetEntity().get();
                Entity caster = context.caster();
                if(!target.isAlive())
                    return false;
                if(target.isSpectator())
                    return false;
                if(target.equals(caster))
                    return false;
                boolean isAlly = caster != null && caster.isAlliedTo(target);

                if (!effect.isBeneficial) {
                    if (target.equals(caster)) return false; // Evita auto-dano
                    if (isAlly && !effect.isFriendlyFire) return false; // Bloqueia dano em aliados se FF estiver desligado
                }
                // Efeito BENÉFICO (Cura/Buff):
                else {
                    if (!isAlly && !target.equals(caster)) return false; // Efeitos benéficos só afetam o atacante ou seus aliados
                }

                return true;
            }
            @Override
            public Object getTargetFromContext(EffectContext context, Effect effect) {
                return context.getTargetEntity().orElse(null);
            }

        },
        ENTITY_UNCHECKED{
            @Override
            public boolean isValidTarget(EffectContext context,Effect effect) {
                return context.getTargetEntity().isPresent();
            }

            @Override
            public Object getTargetFromContext(EffectContext context, Effect effect) {
                return context.getTargetEntity().orElse(null);
            }
        },
        BLOCK{
            @Override
            public boolean isValidTarget(EffectContext context,Effect effect) {
                return context.getTargetBlockPos().isPresent();
            }

            @Override
            public Object getTargetFromContext(EffectContext context, Effect effect) {
                return context.getTargetBlockPos().orElse(null);
            }
        },
        SELF {
            @Override
            public Object getTargetFromContext(EffectContext context, Effect effect) {
                return context.caster();
            }
        };

        public boolean isValidTarget(EffectContext context,Effect effect){
            return true;
        }

        public abstract Object getTargetFromContext(EffectContext context,Effect effect);


    }

}

