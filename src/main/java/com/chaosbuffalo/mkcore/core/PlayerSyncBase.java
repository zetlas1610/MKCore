package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundNBT;

public class PlayerSyncBase implements ISyncObject {

    protected CompositeUpdater updater = new CompositeUpdater();

    protected void addSyncChild(ISyncObject syncObject) {
        updater.add(syncObject);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        updater.setNotifier(notifier);
    }

    @Override
    public boolean isDirty() {
        return updater.isDirty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        updater.deserializeUpdate(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        updater.serializeUpdate(tag);
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        updater.serializeFull(tag);
    }
}
