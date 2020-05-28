package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData<T extends LivingEntity> {

    void attach(T player);

    T getPlayer();

    PlayerAbilityExecutor getAbilityExecutor();

    PlayerKnowledge getKnowledge();

    IStatsModule<T> getStats();

    default boolean consumeMana(float amount) {
        return getStats().consumeMana(amount);
    }

    default CastState startAbility(MKAbility ability) {
        return getAbilityExecutor().startAbility(ability);
    }

    void clone(IMKEntityData<T> previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

}
