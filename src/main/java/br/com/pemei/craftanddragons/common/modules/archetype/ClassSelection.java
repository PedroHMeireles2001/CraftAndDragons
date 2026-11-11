package br.com.pemei.craftanddragons.common.modules.archetype;

import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.OpenClassSelectionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.Map;

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
                    var levels = archetype.getSkillsPerLevel().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(entry -> new OpenClassSelectionPacket.Level(entry.getKey(), entry.getValue().stream()
                                    .sorted(Comparator.comparing(Skill::getId))
                                    .map(ClassSelection::describeSkill)
                                    .toList()))
                            .toList();
                    String key = "archetype." + MODID + "." + id;
                    return new OpenClassSelectionPacket.Option(id,
                            Component.translatableWithFallback(key + ".name", archetype.getName()),
                            Component.translatableWithFallback(key + ".description", archetype.getDescription()), levels);
                })
                .toList();
        ModMessages.sendToPlayer(player, new OpenClassSelectionPacket(options));
        return options.isEmpty() ? 0 : 1;
    }

    private static OpenClassSelectionPacket.SkillInfo describeSkill(Skill skill) {
        Component description = skill.getDescription();
        if (skill instanceof SkillEffect effect && effect.hasUseEffects()) {
            description = Component.translatable("screen.craftanddragons.class_selection.active_skill",
                    effect.getUseManaCost(), description);
        }
        return new OpenClassSelectionPacket.SkillInfo(skill.getName(), description);
    }
}
