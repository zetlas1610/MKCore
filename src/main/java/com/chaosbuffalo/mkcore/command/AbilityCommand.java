package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.PlayerKnownAbilities;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class AbilityCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("ability")
                .then(Commands.literal("learn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .executes(AbilityCommand::learnAbility)))
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .executes(AbilityCommand::unlearnAbility)))
                .then(Commands.literal("list")
                        .executes(AbilityCommand::listAbilities))
                ;
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

    static int unlearnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(cap -> cap.getKnowledge().unlearnAbility(abilityId));

        return Command.SINGLE_SUCCESS;
    }

    static int listAbilities(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();


        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerKnownAbilities knownAbilities = cap.getKnowledge().getKnownAbilities();
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
