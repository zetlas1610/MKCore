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

    default boolean consumeMana(float amount) {
        return getStats().consumeMana(amount);
    }

    default int getAbilityRank(ResourceLocation abilityId) {
        return getKnowledge().getAbilityRank(abilityId);
    }

    default CastState startAbility(PlayerAbility ability) {
        return getAbilityExecutor().startAbility(ability);
    }

    void clone(IMKPlayerData previous, boolean death);

    void update();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
