package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundNBT>> implements ISyncObject {

    private final String rootName;
    private final Supplier<Map<K, V>> mapSupplier;
    private final BiConsumer<V, CompoundNBT> keyEncoder;
    private final Function<CompoundNBT, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> factory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncMapUpdater(String rootName, Supplier<Map<K, V>> mapSupplier, BiConsumer<V, CompoundNBT> keyEncoder, Function<CompoundNBT, K> keyDecoder, Function<K, V> factory) {
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

        if (root.getBoolean("f")) {
            mapSupplier.get().clear();
        }

        ListNBT list = root.getList("l", Constants.NBT.TAG_COMPOUND);
        deserializeList(list);
    }

    private void deserializeList(ListNBT list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entryTag = list.getCompound(i);
//            MKCore.LOGGER.info("update {} {}", i, entryTag);
            K decodedKey = keyDecoder.apply(entryTag);
            V current = mapSupplier.get().computeIfAbsent(decodedKey, factory);
            if (current == null)
                continue;

            if (!current.deserialize(entryTag)) {
                MKCore.LOGGER.error("Failed to deserialize ability update for {}", decodedKey);
                continue;
            }
            mapSupplier.get().put(decodedKey, current);
        }
    }

    private CompoundNBT serializeEntry(V value) {
        CompoundNBT entryTag = value.serialize();
        keyEncoder.accept(value, entryTag);
        return entryTag;
    }

    private ListNBT serializeList(Collection<K> infoCollection) {
        ListNBT list = new ListNBT();
        infoCollection.forEach(key -> {
            V value = mapSupplier.get().get(key);
            CompoundNBT entryTag = serializeEntry(value);
            list.add(entryTag);
        });
        return list;
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty.isEmpty())
            return;

        CompoundNBT root = new CompoundNBT();
        ListNBT list = serializeList(dirty);
        root.put("l", list);
        tag.put(rootName, root);

        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();
        ListNBT list = serializeList(mapSupplier.get().keySet());
        root.putBoolean("f", true);
        root.put("l", list);
        tag.put(rootName, root);

        dirty.clear();
    }

    public INBT serializeStorage() {
        ListNBT list = serializeList(mapSupplier.get().keySet());
        return list;
    }

    public void deserializeStorage(INBT tag) {
        if (tag instanceof ListNBT) {
            ListNBT list = (ListNBT) tag;
            mapSupplier.get().clear();
            deserializeList(list);
        }
    }
}
