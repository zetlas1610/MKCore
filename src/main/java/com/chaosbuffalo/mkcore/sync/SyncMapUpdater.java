package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncMapUpdater<K, V extends IMKSerializable<CompoundNBT>> implements ISyncObject {

    private final String rootName;
    private final Supplier<Map<K, V>> mapSupplier;
    private final Function<K, String> keyEncoder;
    private final Function<String, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> factory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncMapUpdater(String rootName, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyEncoder, Function<String, K> keyDecoder, Function<K, V> factory) {
        this.rootName = rootName;
        this.mapSupplier = mapSupplier;
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.factory = factory;
    }

    public void markDirty(K key) {
        dirty.add(key);
        parentNotifier.notifyUpdate(this);
    }


    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }


    @Override
    public boolean isDirty() {
        return dirty.size() > 0;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = tag.getCompound(rootName);

        for (String key : root.keySet()) {
            K decodedKey = keyDecoder.apply(key);
            V current = mapSupplier.get().computeIfAbsent(decodedKey, factory);
            if (current == null)
                continue;
            CompoundNBT entry = root.getCompound(key);
            if (!current.deserialize(entry)) {
                MKCore.LOGGER.error("Failed to deserialize ability update for {}", decodedKey);
                continue;
            }
            mapSupplier.get().put(decodedKey, current);
        }

    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty.size() > 0) {
            CompoundNBT root = new CompoundNBT();
            dirty.forEach(key -> {
                V value = mapSupplier.get().get(key);
                CompoundNBT nbt = new CompoundNBT();
                value.serialize(nbt);
                root.put(keyEncoder.apply(key), nbt);
            });

            tag.put(rootName, root);

            dirty.clear();
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();
        mapSupplier.get().forEach((name, persona) -> {
            CompoundNBT nbt = new CompoundNBT();
            persona.serialize(nbt);
            root.put(keyEncoder.apply(name), nbt);
        });

        if (root.size() > 0) {
            tag.put(rootName, root);
        }
    }
}
