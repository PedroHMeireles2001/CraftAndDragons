package br.com.pemei.craftanddragons.core.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;
import static br.com.pemei.craftanddragons.core.init.ModItems.DECAPTATION_MAP;

// Atenção ao "Bus.MOD" aqui. Eventos de registro e configuração rodam no barramento do Mod.
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEventHandler {


    public static void loadDictionary() {
        DECAPTATION_MAP.clear();

        for (String entry : Config.HEAD_DROPS.get()) {
            String[] split = entry.split("=");

            if (split.length == 2) {
                try {
                    // 1. Converte as strings ("minecraft:zombie") para ResourceLocations
                    ResourceLocation entityLoc = ResourceLocation.parse(split[0].trim());
                    ResourceLocation itemLoc = ResourceLocation.parse(split[1].trim());

                    // 2. Busca os objetos reais nos registros do jogo
                    EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(entityLoc);
                    Item item = ForgeRegistries.ITEMS.getValue(itemLoc);

                    // 3. Validação de segurança
                    // O registro de itens retorna Items.AIR se não encontrar o item
                    if (item != Items.AIR) {
                        DECAPTATION_MAP.put(entity, item);
                    } else {
                        System.out.println("Notice: Entity or item not found in the mapping: " + entry);
                    }

                } catch (Exception e) {
                    System.out.println("Formatting error while processing ResourceLocation in: " + entry);
                }
            }
        }
        System.out.println("Decapitation dictionary loaded");
    }
    // Chamado na primeira vez que o jogo lê o arquivo de configuração na inicialização
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        // Verifica se a configuração disparando o evento é a sua (útil caso tenha configs separados para Client e Server)
        if (event.getConfig().getSpec() == Config.SPEC) {
            loadDictionary();
            System.out.println("Decapitation dict loaded successfully!");
        }
    }

    // Chamado sempre que o jogador edita o arquivo .toml e salva com o jogo rodando, ou usa o comando /reload
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            loadDictionary();
            System.out.println("Decapitation dict reloaded successfully! ");
        }
    }
}