package com.chaosbuffalo.mkcore.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class InfoCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("info");
    }
}
