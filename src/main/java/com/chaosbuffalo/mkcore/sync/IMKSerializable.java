package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.INBT;

public interface IMKSerializable<T extends INBT> {
    void serialize(T tag);

    boolean deserialize(T tag);
}
