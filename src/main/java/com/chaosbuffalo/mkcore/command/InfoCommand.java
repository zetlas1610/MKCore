package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.PlayerActionBar;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class InfoCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("info")
                .then(Commands.literal("hotbar")
                        .executes(InfoCommand::showActionBar))
                ;
    }

    static int showActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerActionBar actionBar = playerData.getKnowledge().getActionBar();
            TextUtils.sendPlayerChatMessage(player, "Action Bar");
            for (int i = 0; i < actionBar.getCurrentSize(); i++) {
                TextUtils.sendPlayerChatMessage(player, String.format("%d: %s", i, actionBar.getAbilityInSlot(i)));
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
