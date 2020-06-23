package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.util.ResourceLocation;

public interface ISlottedAbilityContainer {

    void setAbilityInSlot(MKAbility.AbilityType type, int index, ResourceLocation abilityId);

    ResourceLocation getAbilityInSlot(MKAbility.AbilityType type, int slot);

    int getCurrentSlotCount(MKAbility.AbilityType type);

    int getMaximumSlotCount(MKAbility.AbilityType type);

    default boolean isSlotUnlocked(MKAbility.AbilityType type, int slot) {
        return slot < getCurrentSlotCount(type);
    }
}
