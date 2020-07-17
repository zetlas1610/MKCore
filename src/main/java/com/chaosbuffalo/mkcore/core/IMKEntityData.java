package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IAbilityKnowledge getKnowledge();

    IStatsModule getStats();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

}
