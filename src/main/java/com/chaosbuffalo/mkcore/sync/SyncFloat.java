package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public class SyncFloat implements ISyncObject {
    String name;
    private float value;
    private boolean dirty;

    public SyncFloat(String name, float value) {
        this.name = name;
        set(value);
    }

    public void set(float value) {
        this.value = value;
        this.dirty = true;
    }

    public void add(float value) {
        set(get() + value);
    }

    public float get() {
        return value;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)) {
            this.value = tag.getFloat(name);
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty) {
            tag.putFloat(name, value);
            dirty = false;
        }
    }
}
