package br.com.pemei.craftanddragons.common.modules.skills;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillAttribute;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Objects;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("skill")
                        .requires(source -> source.hasPermission(2)) // Exige nível de permissão OP 2

                        // /skill add <player> <skill>
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("skill", StringArgumentType.string())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ModSkills.REGISTRY.keySet(), builder))
                                                .executes(ctx -> {
                                                    try {
                                                        return addSkill(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                ctx.getArgument("skill",String.class)
                                                        );
                                                    } catch (Throwable t) {
                                                        // Mudamos para Throwable para capturar TUDO, até Errors pesados do Java
                                                        System.err.println("==== ERRO GRAVE NO COMANDO DE SKILL ====");
                                                        t.printStackTrace();
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                        )

                        // /skill remove <player> <skill>
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("skill", StringArgumentType.string())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ModSkills.REGISTRY.keySet(), builder))
                                                .executes(ctx -> removeSkill(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        ctx.getArgument("skill",String.class)
                                                ))
                                        )
                                )
                        )

                        // /skill clear <player>
                        .then(Commands.literal("clear")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> clearSkills(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )

                        .then(Commands.literal("see") // <-- Abre o literal "see" mas NÃO fecha aqui
                                .then(Commands.argument("skill", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ModSkills.REGISTRY.keySet(), builder))
                                        .executes(context -> debugSkill(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "skill") // Jeito mais seguro de pegar a String no Brigadier
                                        ))
                                )
                        )

                        .then(
                                Commands.literal("list")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes((ctx) -> debugSkill(ctx.getSource(),EntityArgument.getPlayer(ctx,"player")))
                                        )

                        )
                        .then(
                                Commands.literal("effects")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes((ctx) -> debugEffects(ctx.getSource(),EntityArgument.getPlayer(ctx,"player")))
                                        )
                        )
        );
    }

    private static int debugEffects(CommandSourceStack source, ServerPlayer player) {
        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent((cap)->cap.getActiveEffects().forEach(skill -> source.sendSystemMessage(skill.holder().getName())));
        source.sendSystemMessage(Component.literal("Effects listed!"));
        return 1;
    }

    private static int debugSkill(CommandSourceStack source, ServerPlayer player) {
        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent((cap)->cap.getSkills().forEach(skill -> source.sendSystemMessage(skill.getName())));
        source.sendSystemMessage(Component.literal("Skills listed!"));
        return 1;
    }


    private static int debugSkill(CommandSourceStack source, String skillId) {
        Skill skill = ModSkills.REGISTRY.get(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("§cA The skills '" + skillId + "' doest exists!"));
            return 0;
        }

        source.sendSystemMessage(Component.literal("id: "+skill.toString()));
        source.sendSystemMessage(Component.literal("level: " + skill.getLevel()));
        source.sendSystemMessage(Component.literal("name: "+skill.getName()));
        source.sendSystemMessage(Component.literal("description: "+skill.getDescription()));
        if(skill instanceof SkillEffect skillEffect) {
            for (EffectTrigger trigger : skillEffect.getEffectsExecutor().keySet()) {
                List<EffectExecutor> executors = skillEffect.getEffectsExecutor().get(trigger);
                if(Objects.isNull(executors) || executors.isEmpty()) {
                    continue;
                }
                for(EffectExecutor executor : executors) {
                    source.sendSystemMessage(Component.literal("effect (%s): %s".formatted(trigger.toString(), executor.getName())));
                }
            }
        }
        if(skill instanceof SkillAttribute skillAttribute) {
            source.sendSystemMessage(Component.literal("skill attribute: %s".formatted(skillAttribute.getAttribute().getDescriptionId())));
            source.sendSystemMessage(Component.literal("value: %s".formatted(skillAttribute.getValue())));
            source.sendSystemMessage(Component.literal("operation: %s".formatted(skillAttribute.getOperation().toString())));
        }
        return 1;
    }

    private static int addSkill(CommandSourceStack source, ServerPlayer player, String skillId) {
        Skill skill = ModSkills.REGISTRY.get(skillId);

        if (skill == null) {
            source.sendFailure(Component.literal("§cA The skills '" + skillId + "' doest exists!"));
            return 0;
        }

        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(cap -> {
            if (cap.existsSkill(skill)) {
                source.sendFailure(Component.literal("§cThe player " + player.getScoreboardName() + " already have this skill."));
            } else {
                cap.addSkill(skill);
                // Opcional: Notificar o cliente/sincronizar pacotes de rede aqui se necessário
                source.sendSuccess(() -> Component.literal("§aSkill '" + skillId + "' added to " + player.getScoreboardName()), true);
                player.sendSystemMessage(Component.literal("§aYou learn the skill: " + skillId));
            }
        });

        return 1;
    }

    private static int removeSkill(CommandSourceStack source, ServerPlayer player, String skillId) {
        Skill skill = ModSkills.REGISTRY.get(skillId);

        if (skill == null) {
            source.sendFailure(Component.literal("§cA The skills '" + skillId + "' doest exists!"));
            return 0;
        }

        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(cap -> {
            if (!cap.hasSkill(skill.getId())) {
                source.sendFailure(Component.literal("§cThe player " + player.getScoreboardName() + " don´t have this skill."));
            } else {
                cap.removeSkill(skill);
                source.sendSuccess(() -> Component.literal("§eSkill '" + skillId + "' removed from " + player.getScoreboardName()), true);
                player.sendSystemMessage(Component.literal("§cYou forgot the skill: " + skillId));
            }
        });

        return 1;
    }

    private static int clearSkills(CommandSourceStack source, ServerPlayer player) {
        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(cap -> {
            cap.clearSkills();
            source.sendSuccess(() -> Component.literal("§aAll skills from " + player.getScoreboardName() + " are removed."), true);
            player.sendSystemMessage(Component.literal("§cYour skills are cleaned."));
        });

        return 1;
    }
}
