package br.com.pemei.craftanddragons.core.init;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.VorpalEffect;
import br.com.pemei.craftanddragons.common.modules.item.DragonSlayerItem;
import br.com.pemei.craftanddragons.common.modules.item.MagicSword;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<MagicSword> VORPAL_SWORD = ITEMS.register("vorpal_sword",()->new MagicSword(
            Tiers.DIAMOND,7, -2.4F,new Item.Properties().rarity(Rarity.EPIC),Map.of(EffectTrigger.ON_CRIT, List.of(new VorpalEffect())),true));
    public static final RegistryObject<DragonSlayerItem> DRAGON_SLAYER  = ITEMS.register("dragon_slayer", DragonSlayerItem::new);
    public static final RegistryObject<Item> ITEM_EXTRACTOR_ITEM = ITEMS.register("item_sacrifice_table",
            () -> new BlockItem(ModBlocks.ITEM_EXTRACTOR_TABLE.get(), new Item.Properties()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> MAIN_CREATIVE_TAB = CREATIVE_MODE_TABS.register("craft_and_dragons_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.main_tab")) // Nome da aba (adicione no arquivo de idioma .json)
                    .icon(() -> new ItemStack(Items.DRAGON_HEAD))       // Item que aparece em cima da aba
                    .displayItems((parameters, output) -> {
                        output.accept(VORPAL_SWORD.get());
                        output.accept(DRAGON_SLAYER.get());
                        output.accept(ITEM_EXTRACTOR_ITEM.get());
                    })
                    .build()
    );

    public static final Map<EntityType<?>, Item> DECAPTATION_MAP = new HashMap<>();


}
