package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKPlayerData {

    void attach(PlayerEntity player);

    PlayerEntity getPlayer();

    float getMana();

    void setMana(float value);

    default float getHealth() {
        return getPlayer().getHealth();
    }

    default void setHealth(float value) {
        getPlayer().setHealth(value);
    }

    default float getMaxHealth() {
        return getPlayer().getMaxHealth();
    }

    void clone(IMKPlayerData previous);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
