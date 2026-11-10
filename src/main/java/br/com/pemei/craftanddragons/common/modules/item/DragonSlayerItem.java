package br.com.pemei.craftanddragons.common.modules.item;

import br.com.pemei.craftanddragons.common.modules.block.extractor.Extractable;
import br.com.pemei.craftanddragons.core.init.ModEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DragonSlayerItem extends SwordItem implements Extractable {

    public DragonSlayerItem() {
        super(Tiers.DIAMOND, 7, -2.4F, new Item.Properties().rarity(Rarity.EPIC));
    }

    /**
     * Garante que a espada venha com o encantamento oculto ao ser pega no criativo ou por comando
     */
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        aplicarEncantamentoOculto(stack);
        return stack;
    }

    /**
     * Garante que o encantamento seja aplicado assim que o jogador craftar o item
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        aplicarEncantamentoOculto(stack);
    }

    private static void aplicarEncantamentoOculto(ItemStack stack) {
        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.DRAGON_SLAYER.get(), stack) == 0) {
            // Aplica o encantamento em nível 10
            stack.enchant(ModEnchantments.DRAGON_SLAYER.get(), 10);

            // Oculta a renderização padrão de encantamentos do Minecraft (para não mostrar "Dragon Slayer X")
            // stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        }
    }

    /**
     * Renderiza o Lore e exibe APENAS os novos encantamentos adicionados pelo jogador
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Lore visual da Espada
        tooltip.add(Component.literal("§7Lâmina Lendária §8- §cMatadora de Dragões"));
        tooltip.add(Component.empty());

        // Busca todos os encantamentos presentes no NBT do item
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        // Filtra e desenha apenas os encantamentos que NÃO são o Dragon Slayer
        enchantments.forEach((enchantment, enchLevel) -> {
            if (enchantment != ModEnchantments.DRAGON_SLAYER.get()) {
                tooltip.add(enchantment.getFullname(enchLevel));
            }
        });
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Map.of(ModEnchantments.DRAGON_SLAYER.get(), 10);
    }

    @Override
    public boolean canExtract() {
        return true;
    }
}