package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    PlayerAbilityExecutor getAbilityExecutor();

    PlayerKnowledge getKnowledge();

    IStatsModule getStats();

    default CastState startAbility(MKAbility ability) {
        return getAbilityExecutor().startAbility(ability);
    }

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

}
