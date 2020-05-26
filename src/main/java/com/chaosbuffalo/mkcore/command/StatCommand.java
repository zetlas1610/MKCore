package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.PlayerAttributes;
import com.chaosbuffalo.mkcore.core.PlayerStatsModule;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", PlayerStatsModule::getMana, PlayerStatsModule::setMana))
                .then(createSimpleFloatStat("health", PlayerStatsModule::getHealth, PlayerStatsModule::setHealth))
                .then(createAttributeStat("manaregen", PlayerAttributes.MANA_REGEN))
                .then(createAttributeStat("maxmana", PlayerAttributes.MAX_MANA))
                .then(createAttributeStat("cdr", PlayerAttributes.COOLDOWN))
                .then(createAttributeStat("melee_crit", PlayerAttributes.MELEE_CRIT))
                .then(createAttributeStat("magic_damage", PlayerAttributes.MAGIC_ATTACK_DAMAGE))
                .then(createAttributeStat("melee_crit_damage", PlayerAttributes.MELEE_CRITICAL_DAMAGE))
                .then(createAttributeStat("spell_crit", PlayerAttributes.SPELL_CRIT))
                .then(createAttributeStat("spell_crit_damage", PlayerAttributes.SPELL_CRITICAL_DAMAGE))
                .then(createAttributeStat("heal_bonus", PlayerAttributes.HEAL_BONUS))
                .then(createAttributeStat("magic_armor", PlayerAttributes.MAGIC_ARMOR));
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name, Function<PlayerStatsModule, Float> getter, BiConsumer<PlayerStatsModule, Float> setter) {
        Function<PlayerEntity, Integer> getAction = playerEntity -> {
            playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is %f", name, getter.apply(cap.getStats()))));

            return Command.SINGLE_SUCCESS;
        };

        BiFunction<PlayerEntity, Float, Integer> setAction;
        if (setter != null) {
            setAction = (playerEntity, value) -> {
                playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("Setting %s to %f", name, value));
                    setter.accept(cap.getStats(), value);
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is now %f", name, getter.apply(cap.getStats())));
                });
                return Command.SINGLE_SUCCESS;
            };
        } else {
            setAction = (playerEntity, value) -> {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Setting %s is not supported", name));
                return Command.SINGLE_SUCCESS;
            };
        }
        return createCore(name, getAction, setAction);
    }

    static ArgumentBuilder<CommandSource, ?> createAttributeStat(String name, IAttribute attribute) {
        Function<PlayerEntity, Integer> getAction = playerEntity -> {
            IAttributeInstance instance = playerEntity.getAttribute(attribute);
            //noinspection ConstantConditions
            if (instance != null) {
                String value = String.format("%s is %f (%f base)", name, instance.getValue(), instance.getBaseValue());
                TextUtils.sendPlayerChatMessage(playerEntity, value);
            } else {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Attribute %s not found", name));
            }

            return Command.SINGLE_SUCCESS;
        };

        BiFunction<PlayerEntity, Float, Integer> setAction = (playerEntity, value) -> {
            IAttributeInstance instance = playerEntity.getAttribute(attribute);
            //noinspection ConstantConditions
            if (instance != null) {
                instance.setBaseValue(value);
                String output = String.format("%s is now %f (%f base)", name, instance.getValue(), instance.getBaseValue());
                TextUtils.sendPlayerChatMessage(playerEntity, output);
            } else {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Attribute %s not found", name));
            }
            return Command.SINGLE_SUCCESS;
        };


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
