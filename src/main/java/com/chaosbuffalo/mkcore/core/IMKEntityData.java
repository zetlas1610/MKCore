package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    void attach(LivingEntity entity);

    LivingEntity getEntity();

    PlayerAbilityExecutor getAbilityExecutor();

    PlayerKnowledge getKnowledge();

    IStatsModule getStats();

    boolean consumeMana(float amount);

    default CastState startAbility(MKAbility ability) {
        return getAbilityExecutor().startAbility(ability);
    }

    void clone(IMKEntityData previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

}
