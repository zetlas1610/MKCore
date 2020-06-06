package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncGroup implements ISyncObject, ISyncNotifier {
    List<ISyncObject> components = new ArrayList<>();
    private String nestedName = null;
    private final Set<ISyncObject> dirty = new HashSet<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private boolean forceFull = false;

    public SyncGroup(ISyncObject... syncObjects) {
        for (ISyncObject sync : syncObjects) {
            add(sync);
        }
    }

    public SyncGroup(String nestedName, ISyncObject... syncObjects) {
        this(syncObjects);
        this.nestedName = nestedName;
    }

    public void setNestingName(String name) {
        nestedName = name;
    }

    public void add(ISyncObject sync) {
        components.add(sync);
        sync.setNotifier(this);
    }

    public void remove(ISyncObject syncObject) {
        components.remove(syncObject);
        syncObject.setNotifier(ISyncNotifier.NONE);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public void notifyUpdate(ISyncObject syncObject) {
        dirty.add(syncObject);
        parentNotifier.notifyUpdate(this);
    }

    public void forceDirty() {
        forceFull = true;
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public boolean isDirty() {
        return forceFull || !dirty.isEmpty();
    }

    private CompoundNBT getUpdateRootTag(CompoundNBT tag) {
        return nestedName != null ? tag.getCompound(nestedName) : tag;
    }

    private void writeUpdateRootTag(CompoundNBT tag, CompoundNBT filledRoot) {
        if (nestedName != null && filledRoot.size() > 0) {
            tag.put(nestedName, filledRoot);
        }
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = getUpdateRootTag(tag);
        components.forEach(c -> c.deserializeUpdate(root));
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (forceFull) {
            MKCore.LOGGER.info("SyncGroup.serializeUpdate({}) forced full", nestedName);
            serializeFull(tag);
        } else {
            CompoundNBT root = getUpdateRootTag(tag);
            dirty.stream()
                    .filter(ISyncObject::isDirty)
                    .forEach(c -> c.serializeUpdate(root));
            if (root.size() > 0) {
                writeUpdateRootTag(tag, root);
            }
            dirty.clear();
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = getUpdateRootTag(tag);
        components.forEach(c -> c.serializeFull(root));
        writeUpdateRootTag(tag, root);
        dirty.clear();
        forceFull = false;
    }

    @Override
    public String toString() {
        return String.format("SyncGroup[name='%s', components=%d, dirty=%d]", nestedName, components.size(), dirty.size());
    }
}
