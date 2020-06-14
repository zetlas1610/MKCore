package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.MKAttributes;
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

import java.util.function.*;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", PlayerStatsModule::getMana, PlayerStatsModule::setMana))
                .then(createSimpleFloatStat("health", PlayerStatsModule::getHealth, PlayerStatsModule::setHealth))
                .then(createAttributeStat("manaregen", MKAttributes.MANA_REGEN))
                .then(createAttributeStat("maxmana", MKAttributes.MAX_MANA))
                .then(createAttributeStat("cdr", MKAttributes.COOLDOWN))
                .then(createAttributeStat("melee_crit", MKAttributes.MELEE_CRIT))
                .then(createAttributeStat("melee_crit_multiplier", MKAttributes.MELEE_CRIT_MULTIPLIER))
                .then(createAttributeStat("spell_crit", MKAttributes.SPELL_CRIT))
                .then(createAttributeStat("spell_crit_multiplier", MKAttributes.SPELL_CRIT_MULTIPLIER))
                .then(createAttributeStat("heal_bonus", MKAttributes.HEAL_BONUS))
                .then(createAttributeStat("arcane_damage", MKAttributes.ARCANE_DAMAGE))
                .then(createAttributeStat("arcane_resist", MKAttributes.ARCANE_RESISTANCE))
                .then(createAttributeStat("fire_damage", MKAttributes.FIRE_DAMAGE))
                .then(createAttributeStat("fire_resist", MKAttributes.FIRE_RESISTANCE))
                .then(createAttributeStat("frost_damage", MKAttributes.FROST_DAMAGE))
                .then(createAttributeStat("frost_resist", MKAttributes.FROST_RESISTANCE))
                .then(createAttributeStat("nature_damage", MKAttributes.NATURE_DAMAGE))
                .then(createAttributeStat("nature_resist", MKAttributes.NATURE_RESISTANCE))
                .then(createAttributeStat("holy_damage", MKAttributes.HOLY_DAMAGE))
                .then(createAttributeStat("holy_resist", MKAttributes.HOLY_RESISTANCE))
                .then(createAttributeStat("shadow_damage", MKAttributes.SHADOW_DAMAGE))
                .then(createAttributeStat("shadow_resist", MKAttributes.SHADOW_RESISTANCE))
                .then(createAttributeStat("poison_damage", MKAttributes.POISON_DAMAGE))
                .then(createAttributeStat("poison_resist", MKAttributes.POISON_RESISTANCE))
                .then(createAttributeStat("elemental_damage", MKAttributes.ELEMENTAL_DAMAGE))
                .then(createAttributeStat("elemental_resist", MKAttributes.ELEMENTAL_RESISTANCE));
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name, Function<PlayerStatsModule, Float> getter, BiConsumer<PlayerStatsModule, Float> setter) {
        ToIntFunction<PlayerEntity> getAction = playerEntity -> {
            playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is %f", name, getter.apply(cap.getStats()))));

            return Command.SINGLE_SUCCESS;
        };

        ToIntBiFunction<PlayerEntity, Float> setAction;
        if (setter != null) {
            setAction = (playerEntity, value) -> {
                playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("Setting %s to %f", name, value));
                    setter.accept(cap.getStats(), value);
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is now %f",
                            name, getter.apply(cap.getStats())));
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
        ToIntFunction<PlayerEntity> getAction = playerEntity -> {
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

        ToIntBiFunction<PlayerEntity, Float> setAction = (playerEntity, value) -> {
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

    static ArgumentBuilder<CommandSource, ?> createCore(String name, ToIntFunction<PlayerEntity> getterAction, ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> getterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player")))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .requires(s -> s.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()))
                                .executes(ctx -> setterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player"),
                                        FloatArgumentType.getFloat(ctx, "amount")))));
    }
}
