package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKPlayer {

    void attach(PlayerEntity player);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
