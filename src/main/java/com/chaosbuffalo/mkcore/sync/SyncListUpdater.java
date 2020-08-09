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
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncListUpdater(String name, Supplier<List<T>> list, Function<T, INBT> valueEncoder, Function<INBT, T> valueDecoder) {
        this.name = name;
        this.parent = list;
        this.valueDecoder = valueDecoder;
        this.valueEncoder = valueEncoder;
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

        if (root.contains("s")) {
            deserializeSparseListUpdate(root.getList("s", Constants.NBT.TAG_COMPOUND));
        } else if (root.contains("l")) {
            deserializeStorage(root.get("l"));
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirtyEntries.isEmpty())
            return;

        CompoundNBT root = new CompoundNBT();
        root.put("s", serializeSparseList(dirtyEntries));
        tag.put(name, root);
        dirtyEntries.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();

        root.putBoolean("f", true);
        root.put("l", serializeStorage());
        tag.put(name, root);
        dirtyEntries.clear();
    }

    private CompoundNBT makeSparseEntry(int index, T value) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("i", index);
        tag.put("v", valueEncoder.apply(value));
        return tag;
    }

    private ListNBT serializeSparseList(BitSet dirtyEntries) {
        List<T> fullList = parent.get();
        ListNBT list = new ListNBT();
        dirtyEntries.stream().forEach(i -> list.add(makeSparseEntry(i, fullList.get(i))));
        return list;
    }

    private void deserializeFullList(ListNBT list) {
        List<T> parentList = parent.get();
        for (int i = 0; i < list.size(); i++) {
            T decoded = valueDecoder.apply(list.get(i));
            if (i < parentList.size()) {
                parentList.set(i, decoded);
            }
        }
    }

    private void deserializeSparseListUpdate(ListNBT list) {
        List<T> abilityList = parent.get();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            int index = entry.getInt("i");
            T value = valueDecoder.apply(entry.get("v"));
            abilityList.set(index, value);
        }
    }

    public INBT serializeStorage() {
        ListNBT list = new ListNBT();
        parent.get().forEach(r -> list.add(valueEncoder.apply(r)));
        return list;
    }

    public void deserializeStorage(INBT tag) {
        if (tag instanceof ListNBT) {
            ListNBT list = (ListNBT) tag;
            deserializeFullList(list);
        }
    }
}
