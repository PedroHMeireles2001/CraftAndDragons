package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.core.client.ClassSelectionScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record OpenClassSelectionPacket(List<Option> options) {
    public record Option(String id, String name, String description) {}

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeCollection(options, (buf, option) -> {
            buf.writeUtf(option.id());
            buf.writeUtf(option.name());
            buf.writeUtf(option.description());
        });
    }

    public static OpenClassSelectionPacket decode(FriendlyByteBuf buffer) {
        return new OpenClassSelectionPacket(buffer.readList(buf ->
                new Option(buf.readUtf(), buf.readUtf(), buf.readUtf())));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClassSelectionScreen.queue(options));
    }
}
