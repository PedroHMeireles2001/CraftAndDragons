package br.com.pemei.craftanddragons.events;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.core.config.Config;
import br.com.pemei.craftanddragons.core.init.ModAttributes;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.enchantment.DamageTypeEnchantment;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import br.com.pemei.craftanddragons.core.init.ModEntities;
import br.com.pemei.craftanddragons.events.custom.PlayerCritEvent;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.*;

// O barramento FORGE escuta os acontecimentos do mundo em tempo real
@Mod.EventBusSubscriber(modid = "craftanddragons", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttackEvent {
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical() && Config.DISABLE_VANILLA_CRIT.get()) {
            event.setResult(Event.Result.DENY);
        }
    }
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();

        Entity sourceEntity = event.getSource().getEntity();

        if (!target.level().isClientSide() && sourceEntity instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();

            putDefaultType(attacker, target);

            checkTypeEnchantment(event, weapon, target);

            checkWeaponEffects(weapon,target,attacker,EffectTrigger.ON_HIT);
            checkSkillsEffects(attacker,target,EffectTrigger.ON_HIT);

            checkCriticalHit(event, weapon, target, attacker);
        }
    }

    private static void checkCriticalHit(LivingHurtEvent event, ItemStack weapon, LivingEntity target, LivingEntity attacker) {
        if(!Config.ENABLE_MODDED_CRIT.get())
            return;

        if(!(attacker instanceof Player playerAttacker))
            return;

        float result;
        double critMultiplier = ModAttributes.DEFAULT_CRIT_MULTIPLIER;
        double critChance = ModAttributes.DEFAULT_CRIT_CHANCE;

        AttributeInstance chanceAttribute = playerAttacker.getAttribute(ModAttributes.CRIT_CHANCE.get());
        AttributeInstance multiplierAttribute = playerAttacker.getAttribute(ModAttributes.CRIT_MULTIPLIER.get());

        if (Objects.nonNull(chanceAttribute)) {
            critChance = chanceAttribute.getValue();
        }
        if(Objects.nonNull(multiplierAttribute)){
            critMultiplier = multiplierAttribute.getValue();
        }

        result = playerAttacker.getRandom().nextFloat() * 100; //resultado de 1 a 100 igual CoC 7e


        if(result < critChance) {
            double finalDamage = event.getAmount() * critMultiplier;
            PlayerCritEvent customCritEvent = new PlayerCritEvent(playerAttacker,target,result,finalDamage);
            boolean canceled = MinecraftForge.EVENT_BUS.post(customCritEvent);
            if(canceled)
                return;
            event.setAmount((float) finalDamage);
            checkWeaponEffects(weapon,target,attacker,EffectTrigger.ON_CRIT);
            checkSkillsEffects(attacker,target,EffectTrigger.ON_CRIT);
        }
    }


    private static void checkTypeEnchantment(LivingHurtEvent event, ItemStack weapon, LivingEntity target) {
        try{

            Set<EntityCategory> entityCategories = ModEntities.entityTypes.get(target.getType());

            if(Objects.isNull(entityCategories) || entityCategories.isEmpty()){
                System.out.println("Null dictionary");
                return;
            }

            weapon.getAllEnchantments().entrySet().stream()
                    .filter(entry -> entry.getKey() instanceof DamageTypeEnchantment)
                    .map((entry)-> new AbstractMap.SimpleEntry<>((DamageTypeEnchantment)entry.getKey(),entry.getValue()))
                    .filter((entry)-> entry.getKey().getCategory().stream().anyMatch(entityCategories::contains))
                    .max(Comparator.comparingDouble((entry)->entry.getKey().getBonusDamage(entry.getValue())))
                    .ifPresent((entry)->{
                        DamageTypeEnchantment damageTypeEnchantment = entry.getKey();
                        int level = entry.getValue();
                        float bonusDamage = damageTypeEnchantment.getBonusDamage(level);
                        event.setAmount(event.getAmount() + bonusDamage);
                        if (!FMLLoader.isProduction())
                            System.out.println("Aplicado %s Nível %d com +%.1f de dano bônus!".formatted(damageTypeEnchantment.getDescriptionId(), level, bonusDamage));
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkWeaponEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker, EffectTrigger effectTrigger) {
        if(itemStack.getItem() instanceof EffectHolder holder) {
            checkAttackEffects(holder,target,attacker,effectTrigger);
        }
    }

    private static void checkSkillsEffects(LivingEntity caster, LivingEntity target, EffectTrigger trigger) {
        if (caster.level().isClientSide()) {
            return;
        }

        caster.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent((skills) -> {
            skills.applySkillsEffects(trigger, caster, target, 1f);
        });
    }


    private static void checkAttackEffects(EffectHolder holder, LivingEntity target, LivingEntity attacker, EffectTrigger trigger) {
        List<EffectExecutor> onHitEffects = holder.getEffectsExecutor(trigger);
        if(Objects.isNull(onHitEffects) || onHitEffects.isEmpty())
            return;
        Level level = attacker.level();
        if(level.isClientSide())
            return;

        EffectContext context = new EffectContext(level,holder,trigger,attacker,target,null,null,null,1f);

        onHitEffects.forEach(effect -> {effect.apply(context);});

    }

    private static void putDefaultType(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Player player) {
            ModEntities.putDefaultCategories(target);
        }
    }
}
