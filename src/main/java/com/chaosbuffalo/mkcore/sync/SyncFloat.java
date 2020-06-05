package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

public class SyncFloat implements ISyncObject {
    String name;
    private float value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncFloat(String name, float value) {
        this.name = name;
        set(value);
    }

    public void set(float value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
    }

    public void add(float value) {
        set(get() + value);
    }

    public float get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
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
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        tag.putFloat(name, value);
        dirty = false;
    }
}
