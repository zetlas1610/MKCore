package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IAbilityKnowledge getKnowledge();

    IStatsModule getStats();

    default AbilityContext startAbility(AbilityContext context, MKAbility ability) {
        return getAbilityExecutor().startAbility(context, ability);
    }

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

}
