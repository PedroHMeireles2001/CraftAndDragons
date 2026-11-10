package br.com.pemei.craftanddragons.core.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

public class ModAttributes {

    public static final double DEFAULT_CRIT_CHANCE = 0.05D;//0.05D = 1/20
    public static final double DEFAULT_CRIT_MULTIPLIER = 2D;

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);


    // Nome, Valor Padrão (1.5x), Mínimo (1.0x), Máximo (100.0x)
    public static final RegistryObject<Attribute> CRIT_MULTIPLIER = ATTRIBUTES.register("crit_multiplier",
            () -> new RangedAttribute("attribute.name.craftanddragons.crit_chance", DEFAULT_CRIT_MULTIPLIER, 1.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CRIT_CHANCE = ATTRIBUTES.register("crit_chance",
            () -> new RangedAttribute("attribute.name.craftanddragons.crit_chance", DEFAULT_CRIT_CHANCE , 0D, 1D).setSyncable(true));

}