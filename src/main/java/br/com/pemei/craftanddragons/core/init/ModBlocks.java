package br.com.pemei.craftanddragons.core.init;

import br.com.pemei.craftanddragons.common.modules.block.extractor.ItemExtractorMenu;
import br.com.pemei.craftanddragons.common.modules.block.extractor.ItemExtractorTableBlock;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

public class ModBlocks {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "craftanddragons");

    // O IForgeMenuType.create() liga o registro à classe MagicMenu
    public static final RegistryObject<MenuType<ItemExtractorMenu>> ITEM_EXTRACTOR_MENU = MENUS.register("item_sacrifice_table",
            () -> IForgeMenuType.create((windowId, inv, data) -> new ItemExtractorMenu(windowId, inv))
    );
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> ITEM_EXTRACTOR_TABLE = BLOCKS.register("item_sacrifice_table", ItemExtractorTableBlock::new);

}
