package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CooldownCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("cd")
                .then(Commands.literal("new")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                        .executes(CooldownCommand::newTimer)
                                ))
                )
                .then(Commands.literal("list")
                        .executes(CooldownCommand::listTimer)
                )
                .then(Commands.literal("reset")
                        .executes(CooldownCommand::resetTimers)
                );
    }

    static int newTimer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        String name = StringArgumentType.getString(ctx, "name");
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

        MKCore.getPlayer(player).ifPresent(playerData -> {
            playerData.getStats().setTimer(MKCore.makeRL(name), ticks);
            TextUtils.sendPlayerChatMessage(player, String.format("Created timer %s with %d ticks", name, ticks));
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listTimer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getStats().printActiveCooldowns());

        return Command.SINGLE_SUCCESS;
    }

    static int resetTimers(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getStats().resetAllCooldowns());

        return Command.SINGLE_SUCCESS;
    }
}
