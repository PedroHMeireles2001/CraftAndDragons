package br.com.pemei.craftanddragons.events;

import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class ModCapabilityEvents {

    // Registra a classe da Capability no Forge
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerSkillsCapability.class);
    }

    // Anexa a Capability no Player quando ele é gerado
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity) {
            if (!event.getObject().getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).isPresent()) {
                event.addCapability(
                        ResourceLocation.fromNamespaceAndPath("craftanddragons", "player_skills"),
                        new PlayerSkillsCapabilityProvider(entity)
                );
            }
        }
        if(event.getObject() instanceof Player player){
            if (!event.getObject().getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).isPresent()) {
                event.addCapability(
                        ResourceLocation.fromNamespaceAndPath("craftanddragons", "player_class"),
                        new PlayerClassCapabilityProvider(player)
                );
            }
        }
    }

    // Preserva as skills ao morrer ou mudar de dimensão
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                    newStore.reloadSkills();
                });
            });

            event.getOriginal().getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }
}