package com.chaosbuffalo.mkcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class MKCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("mk")
                .then(StatCommand.register())
                .then(CooldownCommand.register())
                .then(AbilityCommand.register())
                .then(EffectCommand.register())
                .then(InfoCommand.register())
                .then(PersonaCommand.register())
                .then(TalentCommand.register())
                .then(HotBarCommand.register());
        dispatcher.register(builder);
    }

}
