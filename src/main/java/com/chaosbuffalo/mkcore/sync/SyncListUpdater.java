package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncListUpdater<T> implements ISyncObject {
    private final Supplier<List<T>> parent;
    private final String name;
    private final Function<T, INBT> valueEncoder;
    private final Function<INBT, T> valueDecoder;
    private final int nbtValueType;
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncListUpdater(String name, Supplier<List<T>> list, Function<T, INBT> valueEncoder, Function<INBT, T> valueDecoder, int nbtValueType) {
        this.name = name;
        this.parent = list;
        this.valueDecoder = valueDecoder;
        this.valueEncoder = valueEncoder;
        this.nbtValueType = nbtValueType;
    }

    private CompoundNBT makeSparseEntry(int index, T value) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("i", index);
        tag.put("v", valueEncoder.apply(value));
        return tag;
    }

    public void setDirty(int index) {
        dirtyEntries.set(index);
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirtyEntries.isEmpty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = tag.getCompound(name);

        if (root.getBoolean("f")) {
            parent.get().clear();
        }

        if (root.contains("l")) {
            deserializeList(root.getList("l", nbtValueType));
        } else if (root.contains("s")) {
            deserializeSparse(root.getList("s", Constants.NBT.TAG_COMPOUND));
        }
    }

    private void deserializeList(ListNBT list) {
        for (int i = 0; i < list.size(); i++) {
            INBT entry = list.get(i);
            T decoded = valueDecoder.apply(entry);
            List<T> abilityList = parent.get();
            if (abilityList != null) {
                abilityList.set(i, decoded);
            }
        }
    }

    private void deserializeSparse(ListNBT list) {
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
        ListNBT sparseList = root.getList(name, Constants.NBT.TAG_COMPOUND);
        dirtyEntries.stream().forEach(i -> sparseList.add(makeSparseEntry(i, fullList.get(i))));
        root.put("s", sparseList);
        tag.put(name, root);
        dirtyEntries.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();

        ListNBT list = new ListNBT();
        parent.get().stream().map(valueEncoder).forEach(list::add);

        root.putBoolean("f", true);
        root.put("l", list);
        tag.put(name, root);
        dirtyEntries.clear();
    }

    public void serializeStorage(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        parent.get().forEach(r -> list.add(valueEncoder.apply(r)));
        tag.put(name, list);
    }

    public void deserializeStorage(CompoundNBT tag) {
        ListNBT list = tag.getList(name, nbtValueType);
        List<T> parentList = parent.get();
        for (int i = 0; i < list.size(); i++) {
            T decoded = valueDecoder.apply(list.get(i));
            if (i < parentList.size()) {
                parentList.set(i, decoded);
            }
        }
    }
}
