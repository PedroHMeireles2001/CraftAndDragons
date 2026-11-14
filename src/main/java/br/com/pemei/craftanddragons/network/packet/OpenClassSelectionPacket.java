package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.core.client.ClassSelectionScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record OpenClassSelectionPacket(List<Option> options) {
    public record Option(String id, String iconPath, Component name, Component description, List<Level> levels) {}
    public record Level(int number, List<SkillInfo> skills) {}
    public record SkillInfo(Component name, Component description) {}

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeCollection(options, (buf, option) -> {
            buf.writeUtf(option.id());
            buf.writeUtf(option.iconPath());
            buf.writeComponent(option.name());
            buf.writeComponent(option.description());
            buf.writeCollection(option.levels(), (levelBuffer, level) -> {
                levelBuffer.writeVarInt(level.number());
                levelBuffer.writeCollection(level.skills(), (skillBuffer, skill) -> {
                    skillBuffer.writeComponent(skill.name());
                    skillBuffer.writeComponent(skill.description());
                });
            });
        });
    }

    public static OpenClassSelectionPacket decode(FriendlyByteBuf buffer) {
        return new OpenClassSelectionPacket(buffer.readList(buf -> new Option(
                buf.readUtf(), buf.readUtf(), buf.readComponent(), buf.readComponent(),
                buf.readList(levelBuffer -> new Level(levelBuffer.readVarInt(),
                        levelBuffer.readList(skillBuffer ->
                                new SkillInfo(skillBuffer.readComponent(), skillBuffer.readComponent())))))));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClassSelectionScreen.queue(options));
    }
}
