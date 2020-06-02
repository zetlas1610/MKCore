package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IStatsModule {

    float getDamageTypeBonus(MKDamageType damageType);

    float getHealBonus();

    float getHealth();

    void setHealth(float value);

    float getMaxHealth();

    int getCurrentAbilityCooldown(ResourceLocation abilityId);

    int getAbilityCooldown(MKAbility ability);

    float getAbilityManaCost(MKAbility ability);

    boolean canActivateAbility(MKAbility ability);

    void setTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    void resetAllTimers();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}