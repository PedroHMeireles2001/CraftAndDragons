package br.com.pemei.craftanddragons.common.modules.block.extractor;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface Extractable {
    Map<Enchantment,Integer> getEnchantments();
    boolean canExtract();
}
