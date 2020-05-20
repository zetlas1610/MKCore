package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public interface ISyncObject {
    boolean isDirty();

    void deserializeUpdate(CompoundNBT tag);

    void serializeUpdate(CompoundNBT tag);
}
