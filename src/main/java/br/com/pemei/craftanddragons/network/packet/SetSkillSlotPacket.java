package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import br.com.pemei.craftanddragons.events.ActiveSkillEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetSkillSlotPacket(int slot, String skillId) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
        buffer.writeUtf(skillId);
    }

    public static SetSkillSlotPacket decode(FriendlyByteBuf buffer) {
        return new SetSkillSlotPacket(buffer.readVarInt(), buffer.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null) return;

        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(skills -> {
            if (skills.setSkillSlot(slot, skillId)) ActiveSkillEvents.sync(player);
        });
    }
}
