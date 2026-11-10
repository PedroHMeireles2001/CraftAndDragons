package br.com.pemei.craftanddragons.common.modules.archetype;

import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.OpenClassSelectionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class ClassSelection {
    private static final String PROMPT_SHOWN = MODID + ".class_selection_shown";

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var data = player.getPersistentData();
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(PROMPT_SHOWN)) return;

        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(capability -> {
            if (capability.getArchetype() == null && open(player) == 0) return;

            persisted.putBoolean(PROMPT_SHOWN, true);
            data.put(Player.PERSISTED_NBT_TAG, persisted);
        });
    }

    public static int open(ServerPlayer player) {
        var options = ModArchetype.getArchetypeNames().stream()
                .sorted()
                .map(id -> {
                    var archetype = ModArchetype.getArchetype(id);
                    return new OpenClassSelectionPacket.Option(id, archetype.getName(), archetype.getDescription());
                })
                .toList();
        ModMessages.sendToPlayer(player, new OpenClassSelectionPacket(options));
        return options.isEmpty() ? 0 : 1;
    }
}
