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

    PlayerStatsModule getStats();

    default float getMana() {
        return getStats().getMana();
    }

    default void setMana(float value) {
        getStats().setMana(value);
    }

    default boolean consumeMana(float amount) {
        return getStats().consumeMana(amount);
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

    void clone(IMKPlayerData previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
