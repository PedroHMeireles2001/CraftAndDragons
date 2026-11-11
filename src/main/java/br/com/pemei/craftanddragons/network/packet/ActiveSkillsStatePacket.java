package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.core.client.ActiveSkillControls;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record ActiveSkillsStatePacket(int mana, int maxMana, List<Entry> skills, List<String> slots) {
    public record Entry(String id, Component name, int manaCost, int secondsRemaining) {}

    public Entry skillInSlot(int slot) {
        String id = slots.get(slot);
        if (id.isEmpty()) return null;
        return skills.stream().filter(skill -> skill.id().equals(id)).findFirst().orElse(null);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(mana);
        buffer.writeVarInt(maxMana);
        buffer.writeCollection(skills, (buf, skill) -> {
            buf.writeUtf(skill.id());
            buf.writeComponent(skill.name());
            buf.writeVarInt(skill.manaCost());
            buf.writeVarInt(skill.secondsRemaining());
        });
        buffer.writeCollection(slots, FriendlyByteBuf::writeUtf);
    }

    public static ActiveSkillsStatePacket decode(FriendlyByteBuf buffer) {
        return new ActiveSkillsStatePacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readList(buf ->
                new Entry(buf.readUtf(), buf.readComponent(), buf.readVarInt(), buf.readVarInt())),
                buffer.readList(FriendlyByteBuf::readUtf));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ActiveSkillControls.setState(this));
    }
}
