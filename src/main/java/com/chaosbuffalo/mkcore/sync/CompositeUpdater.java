package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositeUpdater implements ISyncObject, ISyncNotifier {
    List<ISyncObject> components = new ArrayList<>();
    private String nestedName = null;
    private final Set<ISyncObject> dirty = new HashSet<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public CompositeUpdater(ISyncObject... syncs) {
        for (ISyncObject sync : syncs) {
            add(sync);
        }
    }

    public CompositeUpdater(String nestedName, ISyncObject... syncs) {
        this(syncs);
        this.nestedName = nestedName;
    }

    public void add(ISyncObject sync) {
        components.add(sync);
        sync.setNotifier(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public void markDirty(ISyncObject syncObject) {
        dirty.add(syncObject);
        parentNotifier.markDirty(this);
    }

    @Override
    public boolean isDirty() {
        return !dirty.isEmpty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (nestedName != null) {
            CompoundNBT actualTag = tag.getCompound(nestedName);
            components.forEach(c -> c.deserializeUpdate(actualTag));
        } else {
            components.forEach(c -> c.deserializeUpdate(tag));
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (nestedName != null) {
            CompoundNBT actualTag = new CompoundNBT();
            dirty.stream()
                    .filter(ISyncObject::isDirty)
                    .forEach(c -> c.serializeUpdate(actualTag));
            tag.put(nestedName, actualTag);
        } else {
            dirty.stream()
                    .filter(ISyncObject::isDirty)
                    .forEach(c -> c.serializeUpdate(tag));
        }
        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        if (nestedName != null) {
            CompoundNBT actualTag = new CompoundNBT();
            components.forEach(c -> c.serializeFull(actualTag));
            tag.put(nestedName, actualTag);
        } else {
            components.forEach(c -> c.serializeFull(tag));
        }
    }
}
