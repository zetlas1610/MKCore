package com.chaosbuffalo.mkcore.core.persona;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IPersonaExtension {
    ResourceLocation getName();

    void onPersonaActivated();

    void onPersonaDeactivated();

    CompoundNBT serialize();

    void deserialize(CompoundNBT tag);
}
