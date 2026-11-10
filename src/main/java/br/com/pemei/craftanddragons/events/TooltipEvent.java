package br.com.pemei.craftanddragons.events;

import br.com.pemei.craftanddragons.core.init.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.common.Mod;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipEvent {


    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        // Garante que a descrição vai APENAS para a Vorpal Sword
        if (event.getItemStack().getItem() == ModItems.VORPAL_SWORD.get()) {

            // Adiciona a lore em Roxo e Itálico
            event.getToolTip().add(Component.translatable("tooltip.craftanddragons.vorpal_sword.lore")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

            // Adiciona a mecânica em Cinza
            event.getToolTip().add(Component.translatable("tooltip.craftanddragons.vorpal_sword.mechanic")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
