package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;

public class EffectCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("effect")
                .then(Commands.literal("list")
                        .executes(EffectCommand::listTimer)
                )
                .then(Commands.literal("clear")
                        .executes(EffectCommand::resetTimers)
                );
    }

    static int listTimer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        Collection<EffectInstance> effects = player.getActivePotionEffects();
        if (effects.size() > 0) {
            TextUtils.sendPlayerChatMessage(player, "Active effects:");
            for (EffectInstance instance : player.getActivePotionEffects()) {
                TextUtils.sendPlayerChatMessage(player, String.format("%s: %d", instance.getPotion().getRegistryName(), instance.getDuration()));
            }
        } else {
            TextUtils.sendPlayerChatMessage(player, "No active effects");
        }

        return Command.SINGLE_SUCCESS;
    }

    static int resetTimers(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        player.clearActivePotions();
        TextUtils.sendPlayerChatMessage(player, "Effects cleared");

        return Command.SINGLE_SUCCESS;
    }
}
