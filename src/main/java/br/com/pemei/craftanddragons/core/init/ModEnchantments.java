package br.com.pemei.craftanddragons.core.init;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.effect.implEffects.VorpalEffect;
import br.com.pemei.craftanddragons.common.modules.enchantment.DamageTypeEnchantment;
import br.com.pemei.craftanddragons.common.modules.enchantment.EffectEnchantment;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, "craftanddragons");
    public static final RegistryObject<Enchantment> VORPAL_ENCHANTMENT = ENCHANTMENTS.register("vorpal_enchantment", ()->new EffectEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Map.of(EffectTrigger.ON_HIT, List.of(new VorpalEffect()))));
    public static final RegistryObject<Enchantment> MISANTHROPY = ENCHANTMENTS.register("misanthropy_enchantment",()->new DamageTypeEnchantment(Set.of(EntityCategory.HUMANOID)));
    public static final RegistryObject<Enchantment> HOLY = ENCHANTMENTS.register("holy_enchantment",()->new DamageTypeEnchantment(Set.of(EntityCategory.FIEND,EntityCategory.UNDEAD)));
    public static final RegistryObject<Enchantment> DRAGON_SLAYER = ENCHANTMENTS.register("dragon_slayer_enchantment",()->new DamageTypeEnchantment(Set.of(EntityCategory.FIEND,EntityCategory.UNDEAD),5f));

}
