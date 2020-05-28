package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class PlayerKnowledge extends PlayerSyncBase {

    private final MKPlayerData playerData;

    private final PlayerActionBar actionBar;
    private final PlayerKnownAbilities knownAbilities;

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        actionBar = new PlayerActionBar(this);
        knownAbilities = new PlayerKnownAbilities(this);
        addSyncChild(actionBar);
        addSyncChild(knownAbilities);
    }

    PlayerEntity getPlayer() {
        return playerData.getEntity();
    }

    public PlayerActionBar getActionBar() {
        return actionBar;
    }

    public PlayerKnownAbilities getKnownAbilities() {
        return knownAbilities;
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return knownAbilities.getAbilityInfo(abilityId);
    }

    public void learnAbility(MKAbility ability) {
        if (knownAbilities.learn(ability)) {
            actionBar.tryPlaceOnBar(ability.getAbilityId());
        } else {
            MKCore.LOGGER.info("learnAbility({}) - failure", ability.getAbilityId());
        }
    }

    public void unlearnAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null) {
            MKCore.LOGGER.warn("{} tried to unlearn ability not in registry: {}", getPlayer(), abilityId);
            return;
        }
        if (knownAbilities.unlearn(abilityId)) {
            // FIXME: maybe generalize this
            playerData.getAbilityExecutor().onAbilityUnlearned(ability);
            actionBar.onAbilityUnlearned(ability);
        }
    }

    public void serialize(CompoundNBT tag) {
        knownAbilities.serialize(tag);
        actionBar.serialize(tag);
    }

    public void deserialize(CompoundNBT tag) {
        knownAbilities.deserialize(tag);
        actionBar.deserialize(tag);
    }
}
