package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlayerKnowledge implements IAbilityKnowledge, IPlayerSyncComponentProvider {

    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("knowledge");
    private final PlayerActionBar actionBar;
    private final PlayerAbilityKnowledge knownAbilities;
    private final PlayerTalentKnowledge talentKnowledge;
    private final Map<AbilitySlot, IActiveAbilityContainer> abilitySlotContainers = new HashMap<>();

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        actionBar = new PlayerActionBar(playerData);
        knownAbilities = new PlayerAbilityKnowledge(playerData);
        talentKnowledge = new PlayerTalentKnowledge(playerData);
        addSyncChild(actionBar);
        addSyncChild(knownAbilities);
        addSyncChild(talentKnowledge);
        registerAbilityContainer(AbilitySlot.Basic, actionBar);
        registerAbilityContainer(AbilitySlot.Passive, talentKnowledge.getPassiveContainer());
        registerAbilityContainer(AbilitySlot.Ultimate, talentKnowledge.getUltimateContainer());
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
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

    @Nonnull
    public IActiveAbilityContainer getAbilityContainer(AbilitySlot type) {
        return abilitySlotContainers.getOrDefault(type, IActiveAbilityContainer.EMPTY);
    }

    public void registerAbilityContainer(AbilitySlot type, IActiveAbilityContainer container) {
        abilitySlotContainers.put(type, container);
    }

    public ResourceLocation getAbilityInSlot(AbilitySlot type, int slot) {
        return getAbilityContainer(type).getAbilityInSlot(slot);
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

    public boolean learnAbility(MKAbility ability, ResourceLocation replacingAbilityId) {
        if (!knownAbilities.hasRoomForAbility(ability)) {
            return false;
        }

        if (!replacingAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            if (!unlearnAbility(replacingAbilityId)) {
                return false;
            }
        }

        return learnAbility(ability);
    }

    public boolean learnAbility(MKAbility ability, boolean placeOnBar) {
        if (knownAbilities.learnAbility(ability)) {
            if (placeOnBar) {
                getAbilityContainer(ability.getType().getSlotType()).tryPlaceOnBar(ability.getAbilityId());
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
            getAbilityContainer(ability.getType().getSlotType()).onAbilityUnlearned(ability.getAbilityId());
            return true;
        }
        return false;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return knownAbilities.knowsAbility(abilityId);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("talents", talentKnowledge.serializeNBT());
        tag.put("abilities", knownAbilities.serialize());
        tag.put("action_bar", actionBar.serializeNBT());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        talentKnowledge.deserializeNBT(tag.get("talents"));
        knownAbilities.deserialize(tag.getCompound("abilities"));
        actionBar.deserialize(tag.get("action_bar"));
    }

    public void onPersonaDeactivated() {

    }

    public void onPersonaActivated() {
        actionBar.onPersonaSwitch();
    }
}
