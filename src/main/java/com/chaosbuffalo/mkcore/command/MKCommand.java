package com.chaosbuffalo.mkcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

public class MKCommand {

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
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

    public static void registerArguments() {
        ArgumentTypes.register("ability_id", AbilityIdArgument.class, new ArgumentSerializer<>(AbilityIdArgument::ability));
        ArgumentTypes.register("ability_type", HotBarCommand.AbilityTypeArgument.class, new ArgumentSerializer<>(HotBarCommand.AbilityTypeArgument::abilityType));
//        ArgumentTypes.register("talent_id", TalentCommand.TalentIdArgument.class, new ArgumentSerializer<>(TalentCommand.TalentIdArgument::new));
        ArgumentTypes.register("talent_tree_id", TalentCommand.TalentTreeIdArgument.class, new ArgumentSerializer<>(TalentCommand.TalentTreeIdArgument::talent));
        ArgumentTypes.register("talent_line_id", TalentCommand.TalentLineIdArgument.class, new ArgumentSerializer<>(TalentCommand.TalentLineIdArgument::talentLine));
    }

}
