package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IAbilityKnowledge getKnowledge();

    IStatsModule getStats();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

}
