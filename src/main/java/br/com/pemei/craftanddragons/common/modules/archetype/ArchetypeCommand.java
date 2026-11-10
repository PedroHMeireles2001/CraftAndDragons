package br.com.pemei.craftanddragons.common.modules.archetype;

import br.com.pemei.craftanddragons.common.modules.archetype.base.Archetype;
import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapability;
import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.common.modules.archetype.impl.PrincipalArchetype;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArchetypeCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("rpgclass")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("rpgclass", StringArgumentType.string())
                                                .suggests((ctx, builder)-> SharedSuggestionProvider.suggest(ModArchetype.getArchetypeNames(), builder))
                                                .executes((ctx)-> {
                                                    try{
                                                        return executeCapabilityAction(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx,"player"),
                                                                StringArgumentType.getString(ctx, "rpgclass"),
                                                                "Class %value% added to %player%",
                                                                "Your class is set to %value%",
                                                                PlayerClassCapability::setClass
                                                        );
                                                    } catch (Throwable e) {
                                                        e.printStackTrace();
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                        )
                                )
                        )
                        .then(
                                Commands.literal("status")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes((ctx)-> executePlayerDebug(EntityArgument.getPlayer(ctx,"player")))
                                        )
                        )
                        .then(
                                Commands.literal("skills")
                                        .then(
                                                Commands.argument("rpgclass", StringArgumentType.string())
                                                        .suggests((ctx, builder)-> SharedSuggestionProvider.suggest(ModArchetype.getArchetypeNames(), builder))
                                                        .executes(ctx-> printClass(ctx.getSource(),ctx.getArgument("rpgclass",String.class)))
                                        )
                        )
                        .then(
                                Commands.literal("xp")
                                        .then(Commands.argument("player", EntityArgument.player())) // 1. Adicionado o argumento de player
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                                                .executes(ctx-> executeeCapabilityDoubleAction(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx,"player"),
                                                        DoubleArgumentType.getDouble(ctx, "amount"),
                                                        "§aAdded %value% XP to %player%",
                                                        "§aAdded %value% XP to your main class",
                                                        PlayerClassCapability::giveXp
                                                ))
                                        )
                        )
                        .then(
                                Commands.literal("level")
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                .executes(ctx-> executeeCapabilityIntegerAction(
                                                                        ctx.getSource(),
                                                                        EntityArgument.getPlayer(ctx,"player"),
                                                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                                                        "§aAdded %value% level(s) to %player%",
                                                                        "§aAdded %value% level(s) to your main class",
                                                                        PlayerClassCapability::giveLevel
                                                                ))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int printClass(CommandSourceStack source, String rpgclass) {
        Archetype archetype = ModArchetype.getArchetype(rpgclass);
        if(Objects.isNull(archetype)){
            archetype = ModArchetype.getSubArchetype(rpgclass);
            if(Objects.isNull(archetype)){
                source.sendFailure(Component.literal("§cThe class '" + rpgclass + "' doesnt exist!"));
                return 0;
            }
        }
        source.sendSystemMessage(Component.literal("id: " + archetype.getId()));
        source.sendSystemMessage(Component.literal("name: " + archetype.getName()));
        source.sendSystemMessage(Component.literal("description: " + archetype.getDescription()));
        if(archetype instanceof PrincipalArchetype principalArchetype){
            source.sendSystemMessage(Component.literal("base health: " + principalArchetype.getBaseHealth()));
        }
        source.sendSystemMessage(Component.literal("Skills:"));
        for(int level : archetype.getSkillsPerLevel().keySet()){
            String skills = archetype.getSkillsPerLevel().get(level).stream().map(Skill::getName).map(Objects::toString).collect(Collectors.joining(", "));
            source.sendSystemMessage(Component.literal("level %s: %s".formatted(level, skills)));
        }

        if(archetype instanceof PrincipalArchetype principalArchetype){
            String subclasses = principalArchetype.getSubArchetypes().stream().map(Archetype::getName).collect(Collectors.joining(", "));
            source.sendSystemMessage(Component.literal("subclasses options: %s".formatted(subclasses)));
            source.sendSystemMessage(Component.literal("subclass unlock level: %s".formatted(principalArchetype.getSubClassLevel())));
            if(principalArchetype.isMagic()){
                source.sendSystemMessage(Component.literal("base mana: %s".formatted(principalArchetype.getBaseMana())));
            }
        }
        return 1;
    }


    private static int executePlayerDebug(ServerPlayer player) {
        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap -> {
            player.sendSystemMessage(Component.literal("Main class: " + cap.getArchetype().getName()));
            player.sendSystemMessage(Component.literal("Main sub class: " + (Objects.nonNull(cap.getSubArchetype()) ? cap.getSubArchetype().getName() : "None")));
            player.sendSystemMessage(Component.literal("Current level: " + cap.getClassLevel()));
            player.sendSystemMessage(Component.literal("Current xp %s/%s".formatted(cap.getXp(),cap.xpRequiredForLevel(cap.getClassLevel()+1))));
        });



        return 1;
    }

    private static int executeCapabilityAction(CommandSourceStack source, ServerPlayer player, String value, String adminMessage, String playerMessage, BiConsumer<PlayerClassCapability, PrincipalArchetype> action){
        Optional<PrincipalArchetype> archetypeOpt = Optional.ofNullable(ModArchetype.getArchetype(value));
        if (archetypeOpt.isEmpty()){
            source.sendFailure(Component.literal("§cThe class '" + value + "' doesnt exist!"));
            return 0;
        }
        PrincipalArchetype principalArchetype = archetypeOpt.get();
        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap -> action.accept(cap, principalArchetype));

        // 3. Corrigido para passar o molde correto do playerMessage
        sendMessage(source, player, parseComponent(source, player, value, adminMessage), parseComponent(source, player, value, playerMessage));

        return 1;
    }

    private static int executeeCapabilityDoubleAction(CommandSourceStack source, ServerPlayer player, double amount, String adminMessage, String playerMessage, BiConsumer<PlayerClassCapability, Double> action) {
        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap -> {
            action.accept(cap,amount);
        });

        sendMessage(source,player,parseComponent(source,player, String.valueOf(amount),adminMessage),parseComponent(source,player, String.valueOf(amount),playerMessage));
        return 1;
    }

    private static int executeeCapabilityIntegerAction(CommandSourceStack source, ServerPlayer player, int amount, String adminMessage, String playerMessage, BiConsumer<PlayerClassCapability, Integer> action) {
        player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap -> {
            action.accept(cap,amount);
        });

        sendMessage(source,player,adminMessage,playerMessage);
        return 1;
    }

    private static String parseComponent(CommandSourceStack source, ServerPlayer player, String value, String originalMessage) {
        return originalMessage
                .replace("%value%", value)
                .replace("%player%", player.getScoreboardName())
                .replace("%admin%", source.getTextName());
    }


    private static void sendMessage(CommandSourceStack source, ServerPlayer player, String adminMessage, String playerMessage) {
        if(Objects.isNull(source.getEntity()) || !source.getEntity().getUUID().equals(player.getUUID())) {
            source.sendSystemMessage(Component.literal(adminMessage));
        }
        player.sendSystemMessage(Component.literal(playerMessage));
    }
}