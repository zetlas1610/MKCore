package com.chaosbuffalo.mkcore.abilities.attributes;


import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;

public abstract class AbilityAttribute<T> implements IAbilityAttribute<T> {
    private T currentValue;
    private final String name;

    public AbilityAttribute(String name, T defaultValue){
        currentValue = defaultValue;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getValue() {
        return currentValue;
    }

    @Override
    public void setValue(T newValue) {
        this.currentValue = newValue;
    }

    @Override
    public abstract CompoundNBT serialize();

    @Override
    public abstract void deserialize(CompoundNBT nbt);

    @Override
    public abstract void readFromDataPack(JsonObject obj);
}
