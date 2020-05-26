package com.chaosbuffalo.mkcore.abilities.attributes;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;

public class FloatAttribute extends AbilityAttribute<Float> {

    public FloatAttribute(String name, float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("value", getValue());
        return nbt;
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("value")){
            setValue(nbt.getFloat("value"));
        }
    }

    @Override
    public void readFromDataPack(JsonObject obj) {
        if (obj.has("value")){
            setValue(obj.get("value").getAsFloat());
        }
    }
}
