package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public class SyncInt implements ISyncObject {
    String name;
    private int value;
    private boolean dirty;

    public SyncInt(String name, int value) {
        this.name = name;
        set(value);
    }

    public void set(int value) {
        this.value = value;
        this.dirty = true;
    }

    public void add(int value) {
        set(get() + value);
    }

    public int get() {
        return value;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)) {
            this.value = tag.getInt(name);
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty) {
            tag.putInt(name, value);
            dirty = false;
        }
    }
}
