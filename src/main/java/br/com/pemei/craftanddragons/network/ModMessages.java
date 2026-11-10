package br.com.pemei.craftanddragons.network;

import br.com.pemei.craftanddragons.network.packet.UseSkillPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.tryParse("messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Registra o pacote que criamos
        net.messageBuilder(UseSkillPacket.class, id())
                .decoder(UseSkillPacket::decode)
                .encoder(UseSkillPacket::encode)
                .consumerMainThread(UseSkillPacket::handle)
                .add();
    }

    // Método facilitador para mandar pacotes pro servidor
    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }
}