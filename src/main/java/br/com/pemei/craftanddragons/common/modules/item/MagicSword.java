package br.com.pemei.craftanddragons.common.modules.item;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.block.extractor.Extractable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MagicSword extends SwordItem implements Extractable, MagicItem {

    private final Map<EffectTrigger, List<EffectExecutor>> effects;
    private final boolean canExtract;

    public MagicSword(Tier tier, int damageModifier, float cooldownModifiers, Properties properties, Map<EffectTrigger, List<EffectExecutor>> effects, boolean canExtract) {
        super(tier, damageModifier, cooldownModifiers, properties);
        this.effects = effects;
        this.canExtract = canExtract;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        if(!this.canExtract){
            return Map.of();
        }
        return this.effects.values()
                .stream()
                .flatMap(List::stream)
                .flatMap((effectExecutor) -> effectExecutor.getEffects().stream())
                .map(Effect::getEquivalentEnchantment)
                .filter(Objects::nonNull)
                .map(Supplier::get)
                .collect(Collectors.toMap((e)->e, Enchantment::getMaxLevel));
    }

    @Override
    public boolean canExtract() {
        return this.canExtract;
    }


    @Override
    public List<EffectExecutor> getEffectsExecutor(EffectTrigger trigger) {
        return this.effects.get(trigger);
    }

    @Override
    public Map<EffectTrigger, List<EffectExecutor>> getEffectsExecutor() {
        return this.effects;
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(this);
    }
}
