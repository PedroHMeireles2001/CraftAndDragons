package br.com.pemei.craftanddragons.common.modules.enchantment;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.List;
import java.util.Map;

public class EffectEnchantment extends Enchantment implements EffectHolder {

    private final Map<EffectTrigger, List<EffectExecutor>> effects;
    public EffectEnchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] slots, Map<EffectTrigger, List<EffectExecutor>> effects) {
        super(rarity, enchantmentCategory, slots);
        this.effects = effects;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }


    @Override
    public Map<EffectTrigger, List<EffectExecutor>> getEffectsExecutor() {
        return Map.of();
    }
}
