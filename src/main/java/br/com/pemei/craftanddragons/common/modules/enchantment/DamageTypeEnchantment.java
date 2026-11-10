package br.com.pemei.craftanddragons.common.modules.enchantment;

import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import lombok.Getter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Set;


public class DamageTypeEnchantment extends Enchantment {

    @Getter
    private final Set<EntityCategory> category;
    @Getter
    private final float damageMultiplier;

    public DamageTypeEnchantment(Set<EntityCategory> category) {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.category = category;
        this.damageMultiplier = 2.5f;
    }

    public DamageTypeEnchantment(Set<EntityCategory> category, float damageMultiplier) {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.category = category;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if(other instanceof DamageTypeEnchantment || other.equals(Enchantments.SHARPNESS) || other.equals(Enchantments.SMITE) || other.equals(Enchantments.BANE_OF_ARTHROPODS)) {
            return false;
        }
        return super.checkCompatibility(other);
    }

    public float getBonusDamage(int level) {
        return level*this.damageMultiplier;
    }

}
