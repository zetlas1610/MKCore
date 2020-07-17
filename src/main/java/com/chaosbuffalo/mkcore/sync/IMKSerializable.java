package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.INBT;

public interface IMKSerializable<T extends INBT> {
    T serialize();

    boolean deserialize(T tag);
}
