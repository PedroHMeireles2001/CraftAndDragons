package br.com.pemei.craftanddragons.common.modules.item;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import net.minecraft.world.item.ItemStack;

public interface MagicItem extends EffectHolder {
    public ItemStack getItemStack();
}
