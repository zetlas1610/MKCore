package com.chaosbuffalo.mkcore.sync;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncListUpdater<T> implements ISyncObject {
    private final Supplier<List<T>> parent;
    private final String name;
    private final Function<T, INBT> valueEncoder;
    private final Function<INBT, T> valueDecoder;
    private final IntSet dirtyEntries = new IntOpenHashSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncListUpdater(String name, Supplier<List<T>> list, Function<T, INBT> valueEncoder, Function<INBT, T> valueDecoder) {
        this.name = name;
        this.parent = list;
        this.valueDecoder = valueDecoder;
        this.valueEncoder = valueEncoder;
    }

    private CompoundNBT makeEntry(int index, T value) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("i", index);
        tag.put("v", valueEncoder.apply(value));
        return tag;
    }

    public void setDirty(int index) {
        dirtyEntries.add(index);
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirtyEntries.size() > 0;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = tag.getCompound(name);

        if (root.getBoolean("f")) {
            parent.get().clear();
        }

        ListNBT list = root.getList("l", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            int index = entry.getInt("i");
            T decoded = valueDecoder.apply(entry.get("v"));
            List<T> abilityList = parent.get();
            if (abilityList != null) {
                abilityList.set(index, decoded);
            }
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirtyEntries.isEmpty())
            return;

        CompoundNBT root = new CompoundNBT();
        List<T> fullList = parent.get();
        ListNBT list = tag.getList(name, Constants.NBT.TAG_COMPOUND);
        dirtyEntries.forEach((int i) -> list.add(list.size(), makeEntry(i, fullList.get(i))));

        root.put("l", list);
        tag.put(name, root);
        dirtyEntries.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        List<T> fullList = parent.get();
        CompoundNBT root = new CompoundNBT();
        ListNBT list = tag.getList(name, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fullList.size(); i++) {
            T value = fullList.get(i);
            list.add(i, makeEntry(i, value));
        }

        root.putBoolean("f", true);
        root.put("l", list);
        tag.put(name, root);
        dirtyEntries.clear();
    }

    public void serialize(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        parent.get().forEach(r -> list.add(valueEncoder.apply(r)));
        tag.put(name, list);
    }

    public void deserialize(CompoundNBT tag) {
        ListNBT list = tag.getList(name, Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            T decoded = valueDecoder.apply(list.get(i));
            parent.get().set(i, decoded);
        }
    }
}
