package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

public class PlayerKnowledge extends PlayerSyncComponent implements IAbilityKnowledge {

    private final MKPlayerData playerData;

    private final PlayerActionBar actionBar;
    private final PlayerAbilityKnowledge knownAbilities;
    private final PlayerTalentKnowledge talentKnowledge;

    public PlayerKnowledge(MKPlayerData playerData) {
        super("knowledge");
        this.playerData = playerData;
        actionBar = new PlayerActionBar(playerData);
        knownAbilities = new PlayerAbilityKnowledge(playerData);
        talentKnowledge = new PlayerTalentKnowledge(playerData);
        addChild(actionBar);
        addChild(knownAbilities);
        addChild(talentKnowledge);
    }

    PlayerEntity getPlayer() {
        return playerData.getEntity();
    }

    public PlayerActionBar getActionBar() {
        return actionBar;
    }

    public PlayerAbilityKnowledge getKnownAbilities() {
        return knownAbilities;
    }

    public PlayerTalentKnowledge getTalentKnowledge() {
        return talentKnowledge;
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return knownAbilities.getAbilityInfo(abilityId);
    }

    @Override
    public Collection<MKAbilityInfo> getAbilities() {
        return knownAbilities.getAbilities();
    }

    @Nullable
    @Override
    public MKAbilityInfo getKnownAbilityInfo(ResourceLocation abilityId) {
        return knownAbilities.getKnownAbilityInfo(abilityId);
    }

    @Override
    public boolean learnAbility(MKAbility ability) {
        return learnAbility(ability, ability.getType().canPlaceOnActionBar());
    }

    public boolean learnAbility(MKAbility ability, boolean placeOnBar) {
        if (knownAbilities.learnAbility(ability)) {
            if (placeOnBar) {
                actionBar.tryPlaceOnBar(ability.getAbilityId());
            }
            return true;
        } else {
            MKCore.LOGGER.error("learnAbility({}) for {} failure", ability.getAbilityId(), getPlayer());
            return false;
        }
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null) {
            MKCore.LOGGER.warn("{} tried to unlearn ability not in registry: {}", getPlayer(), abilityId);
            return false;
        }
        if (knownAbilities.unlearnAbility(abilityId)) {
            // FIXME: maybe generalize this
            playerData.getAbilityExecutor().onAbilityUnlearned(ability);
            actionBar.onAbilityUnlearned(ability);
            return true;
        }
        return false;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return knownAbilities.knowsAbility(abilityId);
    }

    public void serialize(CompoundNBT tag) {
        tag.put("talents", talentKnowledge.serializeNBT());
        knownAbilities.serialize(tag);
        actionBar.serialize(tag);
    }

    public void deserialize(CompoundNBT tag) {
        talentKnowledge.deserializeNBT(tag.get("talents"));
        knownAbilities.deserialize(tag);
        actionBar.deserialize(tag);
    }

    public void onPersonaDeactivated() {

    }

    public void onPersonaActivated() {
        actionBar.onPersonaSwitch();
    }
}
