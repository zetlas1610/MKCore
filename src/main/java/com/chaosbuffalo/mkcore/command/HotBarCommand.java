package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.PlayerActionBar;
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

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class HotBarCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("hotbar")
                .then(Commands.literal("show")
                        .executes(HotBarCommand::showActionBar))
                .then(Commands.literal("set")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                        .suggests(HotBarCommand::suggestKnownActiveAbilities)
                                        .executes(HotBarCommand::setActionBar))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                .executes(HotBarCommand::clearActionBar)))
                .then(Commands.literal("reset")
                        .executes(HotBarCommand::resetActionBar))
                .then(Commands.literal("add")
                        .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                .suggests(HotBarCommand::suggestKnownActiveAbilities)
                                .executes(HotBarCommand::addActionBar)))
                ;
    }

    static int setActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();

            if (playerData.getKnowledge().knowsAbility(abilityId)) {
                actionBar.setAbilityInSlot(slot, abilityId);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int addActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();

            if (playerData.getKnowledge().knowsAbility(abilityId)) {
                int slot = actionBar.tryPlaceOnBar(abilityId);
                if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
                    TextUtils.sendChatMessage(player, "No room for ability");
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        int slot = IntegerArgumentType.getInteger(ctx, "slot");

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();
            actionBar.setAbilityInSlot(slot, MKCoreRegistry.INVALID_ABILITY);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int resetActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();
            actionBar.resetBar();
        });

        return Command.SINGLE_SUCCESS;
    }

    static int showActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();
            TextUtils.sendPlayerChatMessage(player, "Action Bar");
            for (int i = 0; i < actionBar.getCurrentSize(); i++) {
                TextUtils.sendChatMessage(player, String.format("%d: %s", i, actionBar.getAbilityInSlot(i)));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static CompletableFuture<Suggestions> suggestKnownActiveAbilities(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getKnowledge()
                                .getKnownAbilities()
                                .getAbilities()
                                .stream()
                                .filter(MKAbilityInfo::isCurrentlyKnown)
                                .filter(info -> info.getAbility().getType().canPlaceOnActionBar())
                                .map(MKAbilityInfo::getId)
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }
}
