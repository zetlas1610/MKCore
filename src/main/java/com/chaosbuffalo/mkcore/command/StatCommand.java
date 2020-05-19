package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;

import java.util.function.Function;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", IMKPlayerData::getMana))
                .then(createSimpleFloatStat("health", IMKPlayerData::getHealth));
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name, Function<IMKPlayerData, Float> getter) {
        Function<PlayerEntity, Integer> getAction = playerEntity -> {
            playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                ITextComponent message = new StringTextComponent(String.format("%s is %.2f", name, getter.apply(cap)));
                message = TextComponentUtils.wrapInSquareBrackets(message);
                playerEntity.sendStatusMessage(message, false);
            });

            return Command.SINGLE_SUCCESS;
        };
        return createCore(name, getAction);
    }

    static ArgumentBuilder<CommandSource, ?> createCore(String name, Function<PlayerEntity, Integer> getterAction) {
        return Commands.literal(name)
                .executes(ctx -> getterAction.apply(ctx.getSource().asPlayer()))
                .then(
                        Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> getterAction.apply(EntityArgument.getPlayer(ctx, "player")))
                );
    }
}
