package com.chaosbuffalo.mkcore.abilities.attributes;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;

public interface IAbilityAttribute<T> {
    T getValue();

    void setValue(T newValue);

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

    String getName();

    void readFromDataPack(JsonObject obj);
}
