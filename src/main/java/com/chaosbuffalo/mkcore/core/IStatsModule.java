package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IStatsModule {

    float getDamageTypeBonus(MKDamageType damageType);

    float getDamageMultiplierForDamageType(MKDamageType damageType);

    float getArmorMultiplierForDamageType(MKDamageType damageType);

    float getHealBonus();

    float getHealth();

    void setHealth(float value);

    float getMaxHealth();

    void tick();

    int getCurrentAbilityCooldown(ResourceLocation abilityId);

    float getActiveCooldownPercent(MKAbilityInfo abilityInfo, float partialTicks);

    int getAbilityCooldown(MKAbility ability);

    boolean canActivateAbility(MKAbility ability);

    void setTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    void printActiveCooldowns();

    void resetAllCooldowns();

    LivingEntity getEntity();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
