package br.com.pemei.craftanddragons.events;

import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapability;
import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class ActiveSkillEvents {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(PlayerClassCapability::reloadClass);
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator()) return;

        if (player.tickCount % 20 == 0) {
            player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(PlayerClassCapability::regenerateMana);
            sync(player);
        }
    }

    public static void sync(ServerPlayer player) {
        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap ->
                player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(skills ->
                        skills.syncActiveSkills(player, cap.getMana(), cap.getMaxMana())));
    }
}
