package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

import java.util.function.BiConsumer;

public class SyncObject<T> implements ISyncObject {
    String name;
    private T value;
    private boolean dirty;
    private final BiConsumer<CompoundNBT, SyncObject<T>> serializer;
    private final BiConsumer<CompoundNBT, SyncObject<T>> deserializer;

    public SyncObject(String name, T value, BiConsumer<CompoundNBT, SyncObject<T>> serializer, BiConsumer<CompoundNBT, SyncObject<T>> deserializer) {
        this.name = name;
        this.serializer = serializer;
        this.deserializer = deserializer;
        set(value);
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
    }

    public T get() {
        return value;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)) {
            deserializer.accept(tag, this);
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty) {
            serializer.accept(tag, this);
            dirty = false;
        }
    }
}
