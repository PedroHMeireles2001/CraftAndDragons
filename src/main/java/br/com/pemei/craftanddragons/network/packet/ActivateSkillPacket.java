package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ActivateSkillPacket(int slot) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
    }

    public static ActivateSkillPacket decode(FriendlyByteBuf buffer) {
        return new ActivateSkillPacket(buffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player != null) {
            player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(skills -> skills.useSkillSlot(slot));
        }
    }
}
