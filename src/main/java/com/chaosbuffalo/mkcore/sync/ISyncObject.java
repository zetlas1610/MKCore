package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public interface ISyncObject {

    void setNotifier(ISyncNotifier notifier);

    boolean isDirty();

    void deserializeUpdate(CompoundNBT tag);

    void serializeUpdate(CompoundNBT tag);

    void serializeFull(CompoundNBT tag);
}
