package com.chaosbuffalo.mkcore.abilities.attributes;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;

public class IntAttribute extends AbilityAttribute<Integer> {

    public IntAttribute(String name, int defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("value", getValue());
        return nbt;
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("value")) {
            setValue(nbt.getInt("value"));
        }
    }

    @Override
    public void readFromDataPack(JsonObject obj) {
        if (obj.has("value")) {
            setValue(obj.get("value").getAsInt());
        }
    }
}
