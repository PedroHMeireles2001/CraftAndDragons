package br.com.pemei.craftanddragons;

import br.com.pemei.craftanddragons.common.modules.addon.JsonAddonLoader;
import br.com.pemei.craftanddragons.common.modules.archetype.ModArchetype;
import br.com.pemei.craftanddragons.core.config.Config;
import br.com.pemei.craftanddragons.core.init.ModAttributes;
import br.com.pemei.craftanddragons.core.init.ModBlocks;
import br.com.pemei.craftanddragons.core.init.ModEnchantments;
import br.com.pemei.craftanddragons.core.init.ModItems;
import br.com.pemei.craftanddragons.network.ModMessages;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Craftanddragons.MODID)
public class Craftanddragons {

    public static final String MODID = "craftanddragons";
    private static final Logger LOGGER = LogUtils.getLogger();


    public Craftanddragons(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        Config.register(context);

        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.MENUS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onCommonSetup);


        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModArchetype.init();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        // Executa o carregamento dos Addons quando as configs já estiverem prontas
        event.enqueueWork(JsonAddonLoader::loadAddons);
        ModMessages.register();
    }

}
