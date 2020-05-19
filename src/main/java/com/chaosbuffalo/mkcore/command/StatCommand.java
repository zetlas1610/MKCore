package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(
                        Commands.argument("player", EntityArgument.player())
                                .executes(StatCommand::get)
                );
    }

    private static int get(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity playerEntity = EntityArgument.getPlayer(ctx, "player");

        MKCore.LOGGER.info("in command get()");

        playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
            MKCore.LOGGER.info("in command get sending status message with mana {}", cap.getMana());
            playerEntity.sendStatusMessage(new StringTextComponent(String.format("mana is %f", cap.getMana())), false);
        });

        return Command.SINGLE_SUCCESS;
    }
}
