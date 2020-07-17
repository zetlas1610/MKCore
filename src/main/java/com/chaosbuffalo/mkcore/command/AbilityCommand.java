package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilitySlotType;
import com.chaosbuffalo.mkcore.core.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AbilityCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("ability")
                .then(Commands.literal("learn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestUnknownActiveAbilities)
                                .executes(AbilityCommand::learnAbility)))
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestKnownActiveAbilities)
                                .executes(AbilityCommand::unlearnAbility)))
                .then(Commands.literal("list")
                        .executes(AbilityCommand::listAbilities))
                .then(Commands.literal("pool_count")
                        .then(Commands.argument("poolCount", IntegerArgumentType.integer())
                                .executes(AbilityCommand::setSlotCount)))
                ;
    }

    public static CompletableFuture<Suggestions> suggestKnownActiveAbilities(final CommandContext<CommandSource> context,
                                                                             final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getKnowledge()
                                .getKnownAbilities()
                                .getAbilities()
                                .stream()
                                .filter(info -> info.getAbility().getType().fitsSlot(AbilitySlotType.Basic))
                                .filter(MKAbilityInfo::isCurrentlyKnown)
                                .map(MKAbilityInfo::getId)
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }

    static CompletableFuture<Suggestions> suggestUnknownActiveAbilities(final CommandContext<CommandSource> context,
                                                                        final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> {
                            Set<MKAbility> allAbilities = new HashSet<>(MKCoreRegistry.ABILITIES.getValues());
                            allAbilities.removeIf(ability -> playerData.getKnowledge().knowsAbility(ability.getAbilityId()));
                            allAbilities.removeIf(ability -> ability.getType().getSlotType() != AbilitySlotType.Basic);
                            return allAbilities.stream().map(MKAbility::getAbilityId).map(ResourceLocation::toString);
                        })
                        .orElse(Stream.empty()),
                builder);
    }

    static int learnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            MKCore.getPlayer(player).ifPresent(cap -> cap.getKnowledge().learnAbility(ability));
        }

        return Command.SINGLE_SUCCESS;
    }

    static int setSlotCount(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int poolCount = IntegerArgumentType.getInteger(ctx, "poolCount");
        MKCore.getPlayer(player).ifPresent(cap -> cap.getKnowledge()
                .getKnownAbilities().setPoolSize(poolCount));
        return Command.SINGLE_SUCCESS;
    }

    static int unlearnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(cap -> cap.getKnowledge().unlearnAbility(abilityId));

        return Command.SINGLE_SUCCESS;
    }

    static int listAbilities(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerAbilityKnowledge knownAbilities = cap.getKnowledge().getKnownAbilities();
            Collection<MKAbilityInfo> abilities = knownAbilities.getAbilities();
            if (abilities.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, "Known Abilities");
                abilities.forEach(info -> TextUtils.sendPlayerChatMessage(player, String.format("%s: %b", info.getId(), info.isCurrentlyKnown())));
            } else {
                TextUtils.sendPlayerChatMessage(player, "No known abilities");
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
