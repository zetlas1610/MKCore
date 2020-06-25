package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IActiveAbilityContainer {

    List<ResourceLocation> getAbilities();

    void setAbilityInSlot(int index, ResourceLocation abilityId);

    default int tryPlaceOnBar(ResourceLocation abilityId) {
        return GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    @Nonnull
    default ResourceLocation getAbilityInSlot(int slot) {
        List<ResourceLocation> list = getAbilities();
        if (slot < list.size()) {
            return list.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    int getCurrentSlotCount();

    int getMaximumSlotCount();

    void clearSlot(int slot);

    void clearAbility(ResourceLocation abilityId);

    default void onAbilityUnlearned(ResourceLocation abilityId) {
        clearAbility(abilityId);
    }

    default boolean isSlotUnlocked(int slot) {
        return slot < getCurrentSlotCount();
    }

    default void resetSlots() {
        for (int i = 0; i < getAbilities().size(); i++) {
            clearSlot(i);
        }
    }

    default int getSlotForAbility(ResourceLocation abilityId) {
        int slot = getAbilities().indexOf(abilityId);
        if (slot != -1)
            return slot;
        return GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    default boolean isAbilitySlotted(ResourceLocation abilityId) {
        return getSlotForAbility(abilityId) != GameConstants.ACTION_BAR_INVALID_SLOT;
    }

    IActiveAbilityContainer EMPTY = new IActiveAbilityContainer() {
        @Override
        public List<ResourceLocation> getAbilities() {
            return Collections.emptyList();
        }

        @Override
        public void setAbilityInSlot(int index, ResourceLocation abilityId) {

        }

        @Nonnull
        @Override
        public ResourceLocation getAbilityInSlot(int slot) {
            return MKCoreRegistry.INVALID_ABILITY;
        }

        @Override
        public int getCurrentSlotCount() {
            return 0;
        }

        public int getMaximumSlotCount() {
            return 0;
        }

        @Override
        public void clearSlot(int slot) {

        }

        @Override
        public void clearAbility(ResourceLocation abilityId) {

        }
    };
}
