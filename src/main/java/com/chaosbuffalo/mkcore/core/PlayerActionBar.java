package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.SyncListUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class PlayerActionBar extends PlayerSyncComponent {

    private final MKPlayerData playerData;
    private final List<ResourceLocation> abilities = NonNullList.withSize(GameConstants.ACTION_BAR_SIZE, MKCoreRegistry.INVALID_ABILITY);
    private final SyncListUpdater<ResourceLocation> hotBarUpdater =
            new SyncListUpdater<>("active",
                    () -> abilities,
                    id -> StringNBT.valueOf(id.toString()),
                    nbt -> new ResourceLocation(nbt.getString()),
                    Constants.NBT.TAG_STRING);

    public PlayerActionBar(MKPlayerData playerData) {
        super("action_bar");
        this.playerData = playerData;
        addPrivate(hotBarUpdater);
    }

    public int getCurrentSize() {
        // TODO: expandable
        return GameConstants.CLASS_ACTION_BAR_SIZE;
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

    public void setAbilityInSlot(int index, ResourceLocation abilityId) {
        if (index < abilities.size()) {
            abilities.set(index, abilityId);
            hotBarUpdater.setDirty(index);
        }
    }

    private void removeFromHotBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            setAbilityInSlot(slot, MKCoreRegistry.INVALID_ABILITY);
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

    public void onAbilityUnlearned(MKAbility ability) {
        removeFromHotBar(ability.getAbilityId());
    }

    private void checkHotBar(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;
        MKAbilityInfo info = playerData.getKnowledge().getKnownAbilityInfo(abilityId);
        if (info != null)
            return;

        MKCore.LOGGER.info("checkHotBar({}) - bad", abilityId);
        removeFromHotBar(abilityId);
    }

    public void serialize(CompoundNBT tag) {
        hotBarUpdater.serializeStorage(tag);
    }

    public void deserialize(CompoundNBT tag) {
        hotBarUpdater.deserializeStorage(tag);
        abilities.forEach(this::checkHotBar);
    }
}
