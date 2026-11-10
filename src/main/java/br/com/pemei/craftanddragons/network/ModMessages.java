package br.com.pemei.craftanddragons.network;

import br.com.pemei.craftanddragons.network.packet.OpenClassSelectionPacket;
import br.com.pemei.craftanddragons.network.packet.SelectClassPacket;
import br.com.pemei.craftanddragons.network.packet.UseSkillPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "2.0";
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.tryParse("messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        INSTANCE = net;

        // Registra o pacote que criamos
        net.messageBuilder(UseSkillPacket.class, id())
                .decoder(UseSkillPacket::decode)
                .encoder(UseSkillPacket::encode)
                .consumerMainThread(UseSkillPacket::handle)
                .add();

        net.messageBuilder(OpenClassSelectionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenClassSelectionPacket::decode)
                .encoder(OpenClassSelectionPacket::encode)
                .consumerMainThread(OpenClassSelectionPacket::handle)
                .add();

        net.messageBuilder(SelectClassPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SelectClassPacket::decode)
                .encoder(SelectClassPacket::encode)
                .consumerMainThread(SelectClassPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    // Método facilitador para mandar pacotes pro servidor
    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }
}
