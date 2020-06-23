package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class PlayerActionBar extends PlayerSyncComponent {

    private final MKPlayerData playerData;
    private final List<ResourceLocation> abilities = NonNullList.withSize(GameConstants.ACTION_BAR_SIZE, MKCoreRegistry.INVALID_ABILITY);
    private final ResourceListUpdater actionBarUpdater = new ResourceListUpdater("active", () -> abilities);

    public PlayerActionBar(MKPlayerData playerData) {
        super("action_bar");
        this.playerData = playerData;
        addPrivate(actionBarUpdater);
    }

    public int getCurrentSize() {
        // TODO: expandable
        return GameConstants.DEFAULT_ACTIVES;
    }

    public int getSlotForAbility(ResourceLocation abilityId) {
        int slot = abilities.indexOf(abilityId);
        if (slot == -1)
            return GameConstants.ACTION_BAR_INVALID_SLOT;
        return slot;
    }

    public ResourceLocation getAbilityInSlot(int slot) {
        if (slot < abilities.size()) {
            return abilities.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    private boolean canSlotKnownAbility(ResourceLocation abilityId) {
        PlayerTalentKnowledge talentKnowledge = playerData.getKnowledge().getTalentKnowledge();
        // TODO: see if this can be less tightly coupled
        if (talentKnowledge.isKnownUltimateAbility(abilityId) && !talentKnowledge.isActiveUltimate(abilityId)) {
            return false;
        }
        return true;
    }

    public void setAbilityInSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("PlayerActionBar.setAbilityInSlot({}, {})", index, abilityId);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            clearSlot(index);
        } else if (playerData.getKnowledge().knowsAbility(abilityId)) {
            if (canSlotKnownAbility(abilityId)) {
                setKnownAbilityInSlot(index, abilityId);
            } else {
                MKCore.LOGGER.error("PlayerActionBar.setAbilityInSlot({}, {}) - tried to slot ultimate that is not slotted!", index, abilityId);
            }
        } else {
            MKCore.LOGGER.error("PlayerActionBar.setAbilityInSlot({}, {}) - tried to slot unknown ability!", index, abilityId);
        }
    }

    private void setKnownAbilityInSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("PlayerActionBar.setAbilityInSlotInternal({}, {})", index, abilityId);
        if (index < abilities.size()) {
            for (int i = 0; i < abilities.size(); i++) {
                if (!abilityId.equals(MKCoreRegistry.INVALID_ABILITY) && i != index && abilityId.equals(abilities.get(i))) {
                    MKCore.LOGGER.info("PlayerActionBar.setAbilityInSlot({}, {}) - moving {} to {}", index, abilityId, abilities.get(i), i);
                    setSlotInternal(i, abilities.get(index));
                }
            }
            setSlotInternal(index, abilityId);
        }
    }

    private void setSlotInternal(int index, ResourceLocation abilityId) {
        abilities.set(index, abilityId);
        actionBarUpdater.setDirty(index);
    }

    private void clearSlot(int index) {
        setSlotInternal(index, MKCoreRegistry.INVALID_ABILITY);
    }

    public void removeFromHotBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            clearSlot(slot);
        }
    }

    private int getFirstFreeAbilitySlot() {
        return getSlotForAbility(MKCoreRegistry.INVALID_ABILITY);
    }

    public int tryPlaceOnBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
            // Skill was just learned so let's try to put it on the bar
            slot = getFirstFreeAbilitySlot();
            if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
                setAbilityInSlot(slot, abilityId);
            }
        }

        return slot;
    }

    public void resetBar() {
        for (int i = 0; i < abilities.size(); i++) {
            clearSlot(i);
        }
    }

    public void onAbilityUnlearned(MKAbility ability) {
        removeFromHotBar(ability.getAbilityId());
    }

    private void checkHotBar(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;
        MKAbilityInfo info = playerData.getKnowledge().getKnownAbilityInfo(abilityId);
        if (info != null)
            return;

        MKCore.LOGGER.debug("checkHotBar({}) - bad", abilityId);
        removeFromHotBar(abilityId);
    }

    void onPersonaSwitch() {
        abilities.forEach(this::checkHotBar);
    }

    public void serialize(CompoundNBT tag) {
        actionBarUpdater.serializeStorage(tag);
    }

    public void deserialize(CompoundNBT tag) {
        actionBarUpdater.deserializeStorage(tag);
    }
}
