package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface ISlottedAbilityContainer {

    void setAbilityInSlot(MKAbility.AbilityType type, int index, ResourceLocation abilityId);

    @Nonnull
    ResourceLocation getAbilityInSlot(MKAbility.AbilityType type, int slot);

    int getCurrentSlotCount(MKAbility.AbilityType type);

    int getMaximumSlotCount(MKAbility.AbilityType type);

    default boolean isSlotUnlocked(MKAbility.AbilityType type, int slot) {
        return slot < getCurrentSlotCount(type);
    }

    ISlottedAbilityContainer EMPTY = new ISlottedAbilityContainer() {
        @Override
        public void setAbilityInSlot(MKAbility.AbilityType type, int index, ResourceLocation abilityId) {

        }

        @Nonnull
        @Override
        public ResourceLocation getAbilityInSlot(MKAbility.AbilityType type, int slot) {
            return MKCoreRegistry.INVALID_ABILITY;
        }

        @Override
        public int getCurrentSlotCount(MKAbility.AbilityType type) {
            return 0;
        }

        public int getMaximumSlotCount(MKAbility.AbilityType type) {
            return 0;
        }
    };
}
