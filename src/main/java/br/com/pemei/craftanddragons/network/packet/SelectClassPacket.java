package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.common.modules.archetype.ModArchetype;
import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectClassPacket(String classId) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(classId);
    }

    public static SelectClassPacket decode(FriendlyByteBuf buffer) {
        return new SelectClassPacket(buffer.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null) return;

        var archetype = ModArchetype.getArchetype(classId);
        if (archetype == null) {
            player.sendSystemMessage(Component.translatable("screen.craftanddragons.class_selection.unavailable"));
            return;
        }

        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(capability -> {
            if (capability.getArchetype() != archetype) {
                capability.setClass(archetype);
            }
            player.sendSystemMessage(Component.translatable(
                    "screen.craftanddragons.class_selection.selected", archetype.getName()));
        });
    }
}
