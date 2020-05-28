package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IStatsModule<T extends LivingEntity> {
    float getCritChanceForDamageType(MKDamageType damageType);

    float getCritMultiplierForDamageType(MKDamageType damageType);

    float getDamageTypeBonus(MKDamageType damageType);

    float getDamageMultiplierForDamageType(MKDamageType damageType);

    float getArmorMultiplierForDamageType(MKDamageType damageType);

    float getMeleeCritChance();

    float getSpellCritChance();

    float getSpellCritDamage();

    float getMeleeCritDamage();

    float getHealBonus();

    float getHealth();

    void setHealth(float value);

    float getMaxHealth();

    float getMana();

    void setMana(float value);

    float getMaxMana();

    void setMaxMana(float max);

    float getManaRegenRate();

    void tick();

    void addMana(float value);

    boolean consumeMana(float amount);

    int getCurrentAbilityCooldown(ResourceLocation abilityId);

    float getActiveCooldownPercent(MKAbilityInfo abilityInfo, float partialTicks);

    int getAbilityCooldown(MKAbility ability);

    float getAbilityManaCost(ResourceLocation abilityId);

    boolean canActivateAbility(MKAbility ability);

    void setTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    void printActiveCooldowns();

    void resetAllCooldowns();

    T getEntity();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
