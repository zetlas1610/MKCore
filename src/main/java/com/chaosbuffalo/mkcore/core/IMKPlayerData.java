package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IMKPlayerData {

    void attach(PlayerEntity player);

    PlayerEntity getPlayer();

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

    void setCooldown(ResourceLocation id, int ticks);

    void setTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    void executeHotBarAbility(int slot);

    ResourceLocation getAbilityInSlot(int slot);

    void clone(IMKPlayerData previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
