package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.PersonaManager;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class PersonaCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("persona")
                .then(Commands.literal("list")
                        .executes(PersonaCommand::listPersonas))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(PersonaCommand::createPersona)))
                .then(Commands.literal("switch")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(PersonaCommand::switchPersona)))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(PersonaCommand::deletePersona)))
                ;
    }

    static int listPersonas(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            PersonaManager personaManager = playerData.getPersonaManager();

            TextUtils.sendPlayerChatMessage(player, "Personas");
            for (String name : personaManager.getPersonaNames()) {
                String pName = name;
                if (personaManager.isPersonaActive(name))
                    pName = pName + " (active)";
                TextUtils.sendChatMessage(player, String.format("%s", pName));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int createPersona(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (personaManager.hasPersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Persona '%s' already exists!", name));
            } else if (personaManager.createPersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Created persona '%s'", name));
            } else {
                TextUtils.sendChatMessage(player, String.format("Unable to create persona '%s'", name));
            }
        });

        return Command.SINGLE_SUCCESS;
    }


    static int switchPersona(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (!personaManager.hasPersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Persona '%s' does not exist!", name));
            } else if (personaManager.activatePersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Activated persona '%s'", name));
            } else {
                TextUtils.sendChatMessage(player, String.format("Unable to activate persona '%s'", name));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int deletePersona(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            String name = StringArgumentType.getString(ctx, "name");

            PersonaManager personaManager = playerData.getPersonaManager();
            if (personaManager.isPersonaActive(name)) {
                TextUtils.sendChatMessage(player, String.format("Unable to delete active persona '%s'", name));
            } else if (!personaManager.hasPersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Persona '%s' does not exists!", name));
            } else if (personaManager.deletePersona(name)) {
                TextUtils.sendChatMessage(player, String.format("Deleted persona '%s'", name));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to delete persona '%s'", name));
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
