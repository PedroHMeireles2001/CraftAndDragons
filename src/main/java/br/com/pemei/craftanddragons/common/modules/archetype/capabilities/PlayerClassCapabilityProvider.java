package br.com.pemei.craftanddragons.common.modules.archetype.capabilities;

import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerClassCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerClassCapability> PLAYER_CLASS = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<PlayerClassCapability> optional = LazyOptional.of(this::getOrCreateCapability);
    private PlayerClassCapability capClass;
    private final Player player;

    public PlayerClassCapabilityProvider(Player player) {
        this.player = player;
    }

    private PlayerClassCapability getOrCreateCapability() {
        if (this.capClass == null) {
            this.capClass = new PlayerClassCapability(this.player);
        }
        return this.capClass;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_CLASS) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return ICapabilityProvider.super.getCapability(cap);
    }



    @Override
    public CompoundTag serializeNBT() {
        return getOrCreateCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateCapability().deserializeNBT(nbt);
    }
}
