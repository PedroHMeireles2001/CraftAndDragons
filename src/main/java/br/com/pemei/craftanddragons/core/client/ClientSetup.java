package br.com.pemei.craftanddragons.core.client;

import br.com.pemei.craftanddragons.common.modules.block.extractor.ItemExtractorScreen;
import br.com.pemei.craftanddragons.core.init.ModBlocks;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "craftanddragons", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ActiveSkillControls.registerKeys(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Enfileira o registro para garantir que as threads do Forge processem corretamente
        event.enqueueWork(() -> {
            MenuScreens.register(ModBlocks.ITEM_EXTRACTOR_MENU.get(), ItemExtractorScreen::new);
        });
    }
}
