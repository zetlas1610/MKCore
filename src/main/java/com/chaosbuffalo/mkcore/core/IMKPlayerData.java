package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IMKPlayerData {

    void attach(PlayerEntity player);

    PlayerEntity getPlayer();

    PlayerAbilityExecutor getAbilityExecutor();

    PlayerKnowledge getKnowledge();

    float getMana();

    void setMana(float value);

    default void addMana(float value) {
        setMana(getMana() + value);
    }

    default boolean consumeMana(float amount) {
        if (getMana() >= amount) {
            setMana(getMana() - amount);
            return true;
        }
        return false;
    }

    default float getMaxMana() {
        return (float) getPlayer().getAttribute(PlayerAttributes.MAX_MANA).getValue();
    }

    void setMaxMana(float max);

    default float getManaRegenRate() {
        return (float) getPlayer().getAttribute(PlayerAttributes.MANA_REGEN).getValue();
    }

    default float getHealth() {
        return getPlayer().getHealth();
    }

    default void setHealth(float value) {
        getPlayer().setHealth(value);
    }

    default float getMaxHealth() {
        return getPlayer().getMaxHealth();
    }

    default int getActionBarSize() {
        return getKnowledge().getActionBarSize();
    }

    void setTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    default ResourceLocation getAbilityInSlot(int slot) {
        return getKnowledge().getAbilityInSlot(slot);
    }

    default int getAbilityRank(ResourceLocation abilityId) {
        return getKnowledge().getAbilityRank(abilityId);
    }

    float getAbilityManaCost(ResourceLocation abilityId);

    default PlayerAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return getKnowledge().getAbilityInfo(abilityId);
    }

    int getCurrentAbilityCooldown(ResourceLocation abilityId);

    float getCooldownPercent(PlayerAbilityInfo abilityInfo, float partialTicks);

    default CastState startAbility(PlayerAbility ability) {
        return getAbilityExecutor().startAbility(ability);
    }

    default boolean isCasting() {
        return getAbilityExecutor().isCasting();
    }

    void clone(IMKPlayerData previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
