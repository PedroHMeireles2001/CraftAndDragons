package br.com.pemei.craftanddragons.common.modules.skills.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSkillsCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerSkillsCapability> PLAYER_SKILLS = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<PlayerSkillsCapability> optional = LazyOptional.of(this::createPlayerSkills);
    private PlayerSkillsCapability skills = null;
    private final LivingEntity entity;

    public PlayerSkillsCapabilityProvider(LivingEntity entity) {
        this.entity = entity; // Salva a entidade recebida no construtor
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_SKILLS) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return ICapabilityProvider.super.getCapability(cap);
    }

    private PlayerSkillsCapability createPlayerSkills() {
        if (this.skills == null) {
            this.skills = new PlayerSkillsCapability(this.entity);
        }
        return this.skills;
    }

    @Override
    public CompoundTag serializeNBT() {
        return createPlayerSkills().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSkills().deserializeNBT(nbt);
    }
}
