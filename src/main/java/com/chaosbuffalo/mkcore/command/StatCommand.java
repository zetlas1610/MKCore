package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", IMKPlayerData::getMana, IMKPlayerData::setMana))
                .then(createSimpleFloatStat("health", IMKPlayerData::getHealth, IMKPlayerData::setHealth));
    }

    static void sendPlayerMessage(PlayerEntity playerEntity, String msg) {
        ITextComponent message = new StringTextComponent(msg);
        message = TextComponentUtils.wrapInSquareBrackets(message);
        playerEntity.sendStatusMessage(message, false);
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name, Function<IMKPlayerData, Float> getter, BiConsumer<IMKPlayerData, Float> setter) {
        Function<PlayerEntity, Integer> getAction = playerEntity -> {
            playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    sendPlayerMessage(playerEntity, String.format("%s is %f", name, getter.apply(cap))));

            return Command.SINGLE_SUCCESS;
        };

        BiFunction<PlayerEntity, Float, Integer> setAction;
        if (setter != null) {
            setAction = (playerEntity, value) -> {
                playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                    sendPlayerMessage(playerEntity, String.format("Setting %s to %f", name, value));
                    setter.accept(cap, value);
                    sendPlayerMessage(playerEntity, String.format("%s is now %f", name, getter.apply(cap)));
                });
                return Command.SINGLE_SUCCESS;
            };
        } else {
            setAction = (playerEntity, value) -> Command.SINGLE_SUCCESS;
        }
        return createCore(name, getAction, setAction);
    }

    static ArgumentBuilder<CommandSource, ?> createCore(String name, Function<PlayerEntity, Integer> getterAction, BiFunction<PlayerEntity, Float, Integer> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> getterAction.apply(EntityArgument.getPlayer(ctx, "player")))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .requires(s -> s.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()))
                                .executes(ctx -> setterAction.apply(EntityArgument.getPlayer(ctx, "player"),
                                        FloatArgumentType.getFloat(ctx, "amount")))));
    }
}
